package org.icc.broadcast.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.dto.AudioByteInfo;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.entity.AudioMeta;
import org.icc.broadcast.entity.BroadcastAudio;
import org.icc.broadcast.entity.ProcessTime;
import org.icc.broadcast.repo.BroadcastAudioRepository;
import org.icc.broadcast.utils.ThreadPoolExecutorFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioPlayService {
    private final PriorityBlockingQueue<AudioByteInfo> concurrentLinkedQueue = new PriorityBlockingQueue<>(100000,
            (o1, o2) -> {
                int c = Math.toIntExact(o1.getTimestamp() - o2.getTimestamp());

                if(c == 0) {
                    c = Math.toIntExact(o1.getSeq() - o2.getSeq());
                }

                return c;
            }
        );

    private static final Executor PERSIST_POOL = ThreadPoolExecutorFactory.get(10000);

    private final BroadcastAudioRepository broadcastAudioRepository;

    private final static int BUFFER_SIZE = 1024;
    private final static int SAMPLE_RATE = 44100;
    private final static int BITS_PER_SAMPLE = 16;
    private final static int CHANNELS = 1;

    private  DataLine.Info info;
    private  SourceDataLine line;


    public void playAudioByte(AudioByteInfo audioByteInfo) {
        concurrentLinkedQueue.put(audioByteInfo);
    }

    public void playAudio(AudioInfo audioInfo) {
        log.info("start to play audio: {}", audioInfo);

        String filePath = audioInfo.getDestFilePath();
        if(!audioInfo.isGenerated() || StringUtils.isBlank(filePath)) {
            filePath = audioInfo.getRawFilePath();
        }

        File destAudioFile = new File(filePath);

        if (!destAudioFile.exists()) {
            log.warn("audio file: {} does not exist", filePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(destAudioFile)) {
            byte[] audioBuffer = new byte[BUFFER_SIZE]; // Define a suitable buffer size
            int bytesRead;

            int seq = 0;
            while ((bytesRead = fis.read(audioBuffer)) != -1) {
                byte []copiedBytes = Arrays.copyOf(audioBuffer, bytesRead);
                AudioByteInfo audioByteInfo = AudioByteInfo.builder()
                        .timestamp(audioInfo.getTimestamp())
                        .seq(seq++)
                        .bytes(copiedBytes)
                        .build();

                concurrentLinkedQueue.put(audioByteInfo);
            }

            PERSIST_POOL.execute(() -> {
                if(!audioInfo.isProcessed() || !audioInfo.isGenerated()) {
                    return;
                }

                BroadcastAudio broadcastAudio = BroadcastAudio.builder()
                        .broadcastId(audioInfo.getBroadcastId())
                        .sessionId(audioInfo.getSessionId())
                        .srcLang(audioInfo.getSrcLang())
                        .rawFilePath(audioInfo.getRawFilePath())
                        .rawText(audioInfo.getRawText())
                        .rawDuration(audioInfo.getRawDuration())
                        .createAt(new Date())
                        .updateTime(new Date())
                        .build();

                List<AudioMeta> audioMetas = Lists.newArrayList(AudioMeta.builder()
                                .audioModel(audioInfo.getDestModel())
                                .lang(audioInfo.getDestLang())
                                .text(audioInfo.getDestText())
                                .duration(audioInfo.getDestDuration())
                                .filePath(audioInfo.getRawDestFilePath())
                                .finalFilePath(audioInfo.getDestFilePath())
                                .provider(audioInfo.getProvider())
                        .build());

                broadcastAudio.setAudioMetas(audioMetas);

                List<ProcessTime> times = Lists.newArrayList(ProcessTime.builder()
                                .type("REC_AND_TRAN")
                                .startTime(new Date(audioInfo.getTextStartTime()))
                                .endTime(new Date(audioInfo.getTextEndTime()))
                                .duration(audioInfo.getTextEndTime() - audioInfo.getTextStartTime())
                        .build(),
                        ProcessTime.builder()
                                .type("SYNTHESISE")
                                .startTime(new Date(audioInfo.getSynthStartTime()))
                                .endTime(new Date(audioInfo.getSynthEndTime()))
                                .duration(audioInfo.getSynthEndTime() - audioInfo.getSynthStartTime())
                                .build()
                        );

                broadcastAudio.setTimes(times);

                broadcastAudioRepository.add(broadcastAudio);
            });

        } catch (IOException e) {
            log.error("read audio: {} bytes error", audioInfo.getDestFilePath(), e);
        }
    }

    @PostConstruct
    public void doPlayAudio() {
        AudioFormat audioFormat = new AudioFormat(
                SAMPLE_RATE, // Sample rate (samples per second)
                BITS_PER_SAMPLE,    // Bits per sample
                CHANNELS,     // Number of channels (1 for mono, 2 for stereo)
                true,  // Signed (true for signed PCM, false for unsigned)
                false  // Big endian (true for big endian, false for little endian)
        );

        try {
            info = new DataLine.Info(SourceDataLine.class, audioFormat);
            line = (SourceDataLine) AudioSystem.getLine(info);

            line.open(audioFormat);
            line.start();

            new Thread(() -> {
                for (;;) {
                    try {
                        AudioByteInfo audioInfo = concurrentLinkedQueue.take();

                        this.doPlayAudio2(audioInfo);
                    } catch (InterruptedException e) {
                        log.error("take audio info error", e);
                    }
                }
            }).start();
        } catch (LineUnavailableException e) {
            log.error("create audio line error", e);

            if(line != null) {
                line.drain(); // Ensures all buffered data is played
                line.stop();
                line.close();
            }
        }
    }

    private void doPlayAudio2(AudioByteInfo audioInfo) {
        byte[] bytes = audioInfo.getBytes();
        line.write(bytes, 0, bytes.length);
    }

}
