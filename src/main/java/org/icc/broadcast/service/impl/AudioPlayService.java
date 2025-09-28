package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioInfo;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioPlayService {
    private PriorityBlockingQueue<AudioInfo> concurrentLinkedQueue = new PriorityBlockingQueue<>(100000, (o1, o2) -> Math.toIntExact(o1.getTimestamp() - o2.getTimestamp()));

    public void playAudio(AudioInfo audioInfo) {
        concurrentLinkedQueue.put(audioInfo);
    }

    @PostConstruct
    public void doPlayAudio() {
        new Thread(() -> {
            for (;;) {
                try {
                    AudioInfo audioInfo = concurrentLinkedQueue.take();

                    this.doPlayAudio(audioInfo);
                } catch (InterruptedException e) {
                    log.error("take audio info error", e);
                }
            }
        }).start();
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
