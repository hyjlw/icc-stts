package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.config.AudioPlayConfig;
import org.icc.broadcast.dto.AudioByteInfo;
import org.icc.broadcast.dto.AudioInfo;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
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

    private final AudioPlayConfig audioPlayConfig;


    public void playAudioByte(AudioByteInfo audioByteInfo) {
        concurrentLinkedQueue.put(audioByteInfo);
    }

    public void playAudio(AudioInfo audioInfo) {
        log.info("start to play audio: {}", audioInfo);

        String filePath = audioInfo.getFinalFilePath();
        if(StringUtils.isBlank(filePath)) {
            filePath = audioInfo.getFilePath();
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

                if(seq < 2) {
                    for(int i = 0; i < copiedBytes.length; i++) {
                        if(copiedBytes[i] > 1) {
                            copiedBytes[i] = 1;
                        }
                    }
                }

                AudioByteInfo audioByteInfo = AudioByteInfo.builder()
                        .timestamp(audioInfo.getTimestamp())
                        .seq(seq++)
                        .bytes(copiedBytes)
                        .build();

                concurrentLinkedQueue.put(audioByteInfo);
            }

            audioFileCount++;

            // default set to 5
            if(audioFileCount > audioPlayConfig.getMinFileCount() && !validToPlay) {
                validToPlay = true;
            }
        } catch (IOException e) {
            log.error("read audio: {} bytes error", filePath, e);
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
