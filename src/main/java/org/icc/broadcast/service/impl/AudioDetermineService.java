package org.icc.broadcast.service.impl;

import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.config.SttsConfig;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${audio.save.path}")
    private String audioPath;

    public void determineAudio(AudioInfo audioInfo) {
        log.info("start to determine audio: {}", audioInfo);

        // set raw duration first;
//        audioInfo.setRawDuration(ffmpegService.getDuration(audioInfo.getRawFilePath()));

        if(sttsConfig.isSttsStarted()) {
            audioTranslationService.translateAudio(audioInfo);
        } else {
            double atempo = 1.1;
            String sessionId = audioInfo.getSessionId();
            String rawFilePath = audioInfo.getRawFilePath();
            String fileName = FileUtil.getName(rawFilePath);

            String destShortenedFilePath = this.audioPath + "/" + sessionId + "/" + "shortened_" + fileName;
            ffmpegService.stretchAudio(rawFilePath, destShortenedFilePath, atempo);

            if (FileUtil.exist(destShortenedFilePath)) {
                audioInfo.setDestFilePath(destShortenedFilePath);

                long destDurationForGenedFile = ffmpegService.getDuration(destShortenedFilePath);
                audioInfo.setDestDuration(destDurationForGenedFile);
            }

            audioPlayService.playAudio(audioInfo);
        }
    }
}
