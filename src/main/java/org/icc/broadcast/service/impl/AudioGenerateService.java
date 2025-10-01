package org.icc.broadcast.service.impl;

import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.utils.ThreadPoolExecutorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class AudioGenerateService {

    private static final Executor SYNTH_POOL = ThreadPoolExecutorFactory.get(10000);

    @Value("${audio.trans.path}")
    private String transPath;

    private final SpeechRecognitionService speechRecognitionService;
    private final AudioPlayService audioPlayService;
    private final FfmpegService ffmpegService;

    public void generateAudio(AudioInfo audioInfo) {
        log.info("start to generate audio: {}", audioInfo);

        String destLang = audioInfo.getDestLang();
        String sessionId = audioInfo.getSessionId();
        String audioModel = audioInfo.getDestModel();

        SYNTH_POOL.execute(() -> {
            try {
                String fileName = FileUtil.getName(audioInfo.getRawFilePath());

                String destFilePath = this.transPath + "/" + sessionId + "/" + fileName;
                String destParentDir = FileUtil.getParent(destFilePath, 1);
                if (!FileUtil.exist(destParentDir)) {
                    try {
                        log.info("create folder: {}", destParentDir);
                        Files.createDirectories(Path.of(destParentDir));
                    } catch (IOException e) {
                        log.error("create dir err: ", e);
                        return;
                    }
                }

                audioInfo.setSynthStartTime(System.currentTimeMillis());

//                speechRecognitionService.synthesizeTextToSpeech(destLang, audioModel, audioInfo.getDestText(), destFilePath);
                speechRecognitionService.synthesizeTextToSpeechSsml(destLang, audioModel, audioInfo.getDestText(), destFilePath);

                audioInfo.setSynthEndTime(System.currentTimeMillis());
                log.info("time elapsed for synthesis: {} ms", (audioInfo.getSynthEndTime() - audioInfo.getSynthStartTime()));

                if (!FileUtil.exist(destFilePath)) {
                    log.warn("generate audio dest: {} file: {} failed", destLang, destFilePath);
                    return;
                }

                audioInfo.setDestFilePath(destFilePath);

                // set dest duration first;
//                long destDuration = ffmpegService.getDuration(destFilePath);
//                audioInfo.setDestDuration(destDuration);

                audioInfo.setGenerated(true);
                audioInfo.setProcessed(true);
            } finally {
                audioPlayService.playAudio(audioInfo);
            }
        });
    }
}
