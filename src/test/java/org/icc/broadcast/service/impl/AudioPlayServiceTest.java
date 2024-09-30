package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AudioPlayServiceTest {

    private final static Executor POOL = ThreadPoolExecutorFactory.getSingle(1000);

    @Resource
    private FfmpegService ffmpegService;

    @Test
    public void testPlay() throws InterruptedException {

        String []files = new String[]{
                "C:\\dev\\trans\\23456789231\\voice_1727237766102.wav",
                "C:\\dev\\trans\\23456789231\\voice_1727237781163.wav",
                "C:\\dev\\trans\\23456789231\\voice_1727237796235.wav",
                "C:\\dev\\trans\\23456789231\\voice_1727237826365.wav",
                "C:\\dev\\trans\\23456789231\\voice_1727237841429.wav",
        };

        for(String filePath : files) {

            POOL.execute(() -> {
                long duration = ffmpegService.getDuration(filePath);

                log.info("play file: {}, duration: {} ms", filePath, duration);

                try {
                    Clip clip = AudioSystem.getClip();

                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
                    clip.open(audioInputStream);

                    clip.start();

                    // sleep
                    TimeUnit.MILLISECONDS.sleep(duration);

                    clip.stop();
                    clip.close();
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException | InterruptedException e) {
                    log.warn("play audio error: {}", e.getMessage());
                }
            });
        }

        // sleep
        TimeUnit.SECONDS.sleep(100);
    }

}
