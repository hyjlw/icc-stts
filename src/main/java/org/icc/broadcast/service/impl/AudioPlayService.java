package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioTransDto;
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

    public void playAudio(AudioTransDto audioTrans, String filePath) {
        log.info("start to play audio: {} to {}", filePath, audioTrans.getDestLang());

        String srcLang = audioTrans.getSrcLang();
        String destLang = audioTrans.getDestLang();
        String sessionId = audioTrans.getSessionId();
        String audioModel = audioTrans.getDestModel();

        SINGLE_POOL.execute(() -> {
            try {
                Clip clip = AudioSystem.getClip();

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
                clip.open(audioInputStream);

                clip.start();

                // sleep
                TimeUnit.MILLISECONDS.sleep(5000);

                clip.stop();
                clip.close();
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
