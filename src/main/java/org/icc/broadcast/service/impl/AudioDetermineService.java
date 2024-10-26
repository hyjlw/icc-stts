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

    private final AudioTranslationService audioTranslationService;
    private final AudioPlayService audioPlayService;

    private final FfmpegService ffmpegService;

    private final SttsConfig sttsConfig;

    public void determineAudio(AudioInfo audioInfo) {
        log.info("start to determine audio: {}", audioInfo);

        // set raw duration first;
        audioInfo.setRawDuration(ffmpegService.getDuration(audioInfo.getRawFilePath()));

        if(sttsConfig.isSttsStarted()) {
            audioTranslationService.translateAudio(audioInfo);
        } else {
            audioPlayService.playAudio(audioInfo);
        }
    }
}
