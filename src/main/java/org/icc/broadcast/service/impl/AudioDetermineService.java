package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.config.SttsConfig;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioDetermineService {

    private static final Executor SINGLE_POOL = ThreadPoolExecutorFactory.getSingle(1000);

    private final AudioTranslationService audioTranslationService;
    private final AudioPlayService audioPlayService;

    private final SttsConfig sttsConfig;

    public void determineAudio(AudioInfo audioInfo) {
        log.info("start to determine audio: {}", audioInfo);

        if(sttsConfig.isSttsStarted()) {
            audioTranslationService.translateAudio(audioInfo);
        } else {
            audioPlayService.playAudio(audioInfo);
        }
    }
}
