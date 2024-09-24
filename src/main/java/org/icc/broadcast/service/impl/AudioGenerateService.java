package org.icc.broadcast.service.impl;


import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
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

    private static final Executor SINGLE_POOL = ThreadPoolExecutorFactory.getSingle(1000);

    @Value("${audio.trans.path}")
    private String transPath;

    private final SpeechRecognitionService speechRecognitionService;
    private final AudioPlayService audioPlayService;


    public void generateAudio(AudioTransDto audioTrans, String fileName, String text) {
        log.info("start to generate audio: {} to {}", text, audioTrans.getDestLang());

        String srcLang = audioTrans.getSrcLang();
        String destLang = audioTrans.getDestLang();
        String sessionId = audioTrans.getSessionId();
        String audioModel = audioTrans.getDestModel();

        SINGLE_POOL.execute(() -> {
            String destFilePath = this.transPath + "/" + sessionId + "/" + fileName;
            String destParentDir = FileUtil.getParent(destFilePath, 1);
            if(!FileUtil.exist(destParentDir)) {
                try {
                    log.info("create folder: {}", destParentDir);
                    Files.createDirectories(Path.of(destParentDir));
                } catch (IOException e) {
                    log.error("create dir err: ", e);
                    return;
                }
            }

            speechRecognitionService.synthesizeTextToSpeech(destLang, audioModel, text, destFilePath);
            if(!FileUtil.exist(destFilePath)) {
                log.warn("generate audio dest: {} file: {} failed", destLang, destFilePath);
                return;
            }

            audioPlayService.playAudio(audioTrans, destFilePath);

        });
    }
}
