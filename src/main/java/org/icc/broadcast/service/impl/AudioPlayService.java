package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final static int BUFFER_SIZE = 1024;
    private final static int SAMPLE_RATE = 24000;
    private final static int BITS_PER_SAMPLE = 16;
    private final static int CHANNELS = 1;

    private  DataLine.Info info;
    private  SourceDataLine line;


    public void playAudioByte(AudioByteInfo audioByteInfo) {
        concurrentLinkedQueue.put(audioByteInfo);
    }

    public void playAudio(AudioInfo audioInfo) {
        log.info("start to play audio: {}", audioInfo);

        File destAudioFile = new File(audioInfo.getDestFilePath());

        if (!destAudioFile.exists()) {
            log.warn("audio file: {} does not exist", audioInfo.getDestFilePath());
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

    private void doPlayAudio21(AudioInfo audioInfo) {
        log.info("start to play audio2: {}", audioInfo);

        File mp3File = new File(audioInfo.getDestFilePath());

        if (!mp3File.exists()) {
            log.warn("audio file: {} does not exist", audioInfo.getDestFilePath());
            return;
        }

        try (FileInputStream fis = new FileInputStream(mp3File)) {
            byte[] audioBuffer = new byte[BUFFER_SIZE]; // Define a suitable buffer size
            int bytesRead;

            while ((bytesRead = fis.read(audioBuffer)) != -1) {
                line.write(audioBuffer, 0, bytesRead);
            }

        } catch (IOException e) {
            log.error("read audio: {} bytes error", audioInfo.getDestFilePath(), e);
        }

    }

    private void doPlayAudio(AudioInfo audioInfo) {
        log.info("start to play audio: {}", audioInfo);

        String destFilePath = audioInfo.getDestFilePath();
        String rawFilePath = audioInfo.getRawFilePath();
        long rawDuration = audioInfo.getRawDuration();
        long destDuration = audioInfo.getDestDuration();

        try {
            if (audioInfo.isProcessed()) {
                if (!audioInfo.isGenerated()) {
                    return;
                }

                Clip clip = AudioSystem.getClip();

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(destFilePath));
                clip.open(audioInputStream);

                log.info("start to play audio: {}, raw duration: {} ms, dest duration: {} ms", destFilePath, rawDuration, destDuration);
                clip.start();

                // sleep
                TimeUnit.MILLISECONDS.sleep(destDuration);

                clip.stop();
                clip.close();
            } else {
                Clip clip = AudioSystem.getClip();

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(rawFilePath));
                clip.open(audioInputStream);

                log.info("start to play original audio: {}, raw duration: {} ms", rawFilePath, rawDuration);
                clip.start();

                // sleep
                TimeUnit.MILLISECONDS.sleep(destDuration);

                clip.stop();
                clip.close();
            }
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException | InterruptedException e) {
            log.warn("play audio: {} error: {}", destFilePath, e.getMessage());
        }
    }
}
