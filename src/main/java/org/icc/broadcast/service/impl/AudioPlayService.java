package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class AudioPlayService {

    private static final Executor SINGLE_POOL = ThreadPoolExecutorFactory.getSingle(1000);

    public void playAudio(AudioInfo audioInfo) {
        log.info("start to play audio: {}", audioInfo);

        String destFilePath = audioInfo.getDestFilePath();
        SINGLE_POOL.execute(() -> {
            try {
                if(audioInfo.isGenerated()) {
                    Clip clip = AudioSystem.getClip();

                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(destFilePath));
                    clip.open(audioInputStream);

                    log.info("start to play audio: {}, raw duration: {} ms, dest duration: {} ms", destFilePath, audioInfo.getRawDuration(), audioInfo.getDestDuration());
                    clip.start();

                    // sleep
                    TimeUnit.MILLISECONDS.sleep(audioInfo.getRawDuration());

                    clip.stop();
                    clip.close();
                } else {
                    // sleep
                    TimeUnit.MILLISECONDS.sleep(audioInfo.getRawDuration());
                }
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException | InterruptedException e) {
                log.warn("play audio: {} error: {}", destFilePath, e.getMessage());
            }
        });
    }
}
