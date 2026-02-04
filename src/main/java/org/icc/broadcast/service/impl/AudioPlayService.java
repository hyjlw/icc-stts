package org.icc.broadcast.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.dto.AudioByteInfo;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.entity.AudioMeta;
import org.icc.broadcast.entity.BroadcastAudio;
import org.icc.broadcast.entity.ProcessTime;
import org.icc.broadcast.repo.BroadcastAudioRepository;
import org.icc.broadcast.utils.SpringContextHolder;
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
import java.util.concurrent.TimeUnit;

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

    private final static int BUFFER_SIZE = 1280;
    private final static int SAMPLE_RATE = 16000;
    private final static int BITS_PER_SAMPLE = 16;
    private final static int CHANNELS = 2;

    private  DataLine.Info info;
    private  SourceDataLine line;

    @Setter
    private int audioFileCount = 0;
    @Setter
    private volatile boolean validToPlay = false;

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

        int seq = 0;

        // set 20ms zero data
        byte[] zeroAudioBytes = new byte[BUFFER_SIZE];
        AudioByteInfo zeroAudioByteInfo = AudioByteInfo.builder()
                .timestamp(audioInfo.getTimestamp())
                .seq(seq++)
                .bytes(zeroAudioBytes)
                .build();
        concurrentLinkedQueue.put(zeroAudioByteInfo);

        try (FileInputStream fis = new FileInputStream(destAudioFile)) {
            byte[] audioBuffer = new byte[BUFFER_SIZE]; // Define a suitable buffer size
            int bytesRead;
            while ((bytesRead = fis.read(audioBuffer)) != -1) {
                byte []copiedBytes = Arrays.copyOf(audioBuffer, bytesRead);
                AudioByteInfo audioByteInfo = AudioByteInfo.builder()
                        .timestamp(audioInfo.getTimestamp())
                        .seq(seq++)
                        .bytes(copiedBytes)
                        .build();

                concurrentLinkedQueue.put(audioByteInfo);
            }

            audioFileCount++;

            if(audioFileCount > 5 && !validToPlay) {
                validToPlay = true;
            }
        } catch (IOException e) {
            log.error("read audio: {} bytes error", audioInfo.getDestFilePath(), e);
        }

        this.saveAudioInfo(audioInfo);
    }

    private void saveAudioInfo(AudioInfo audioInfo) {
        log.info("start to save audio: {}", audioInfo);

        // save the audio info
        PERSIST_POOL.execute(() -> {
            if(!audioInfo.isProcessed() || !audioInfo.isGenerated()) {
                return;
            }

            String rawText = "";
            SpeechRecognitionService speechRecognitionService = SpringContextHolder.getBean(SpeechRecognitionService.class);
            if(speechRecognitionService != null) {
                String recRawText = speechRecognitionService.recognizeFromSpeech(audioInfo.getSrcLang(), audioInfo.getRawFilePath(), false);
                if(!StringUtils.isBlank(recRawText)) {
                    rawText = recRawText;
                }
            }

            BroadcastAudio broadcastAudio = BroadcastAudio.builder()
                    .broadcastId(audioInfo.getBroadcastId())
                    .sessionId(audioInfo.getSessionId())
                    .srcLang(audioInfo.getSrcLang())
                    .rawFilePath(audioInfo.getRawFilePath())
                    .rawText(rawText)
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
                    if(!validToPlay) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            log.error("sleep error");
                        }

                        continue;
                    }
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

        // fix illegal request to write non-integral number of frames (874 bytes, frameSize = 4 bytes)

        int rest = bytes.length % 4;
        if(rest > 0) {
            int len = bytes.length + rest;
            byte []newBytes = new byte[len];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);

            for(int i = 0; i < rest; i++) {
                newBytes[len - i - 1] = 0;
            }

            bytes = newBytes;
        }

        line.write(bytes, 0, bytes.length);
    }

}
