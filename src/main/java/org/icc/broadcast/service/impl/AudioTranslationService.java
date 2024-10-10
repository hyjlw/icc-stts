package org.icc.broadcast.service.impl;

import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioTranslationService {

    private static final Executor SINGLE_POOL = ThreadPoolExecutorFactory.getSingle(1000);

    private final FfmpegService ffmpegService;
    private final SpeechRecognitionService speechRecognitionService;

    private final AudioGenerateService audioGenerateService;
    private final AudioPlayService audioPlayService;

    public void translateAudio(AudioInfo audioInfo) {
        log.info("start to translate audio: {}", audioInfo);

        String srcLang = audioInfo.getSrcLang();
        String destLang = audioInfo.getDestLang();
        String filePath = audioInfo.getRawFilePath();

        SINGLE_POOL.execute(() -> {
            boolean processed = false;

            try {
                // set raw duration first;
                audioInfo.setRawDuration(ffmpegService.getDuration(filePath));

                String parentDir = FileUtil.getParent(filePath, 1);
                String fileName = FileUtil.getName(filePath);

                String convFileName = "conved_" + fileName;
                String convFilePath = parentDir + "/" + convFileName;

                ffmpegService.convertToWavS16(filePath, convFilePath);

                if (!FileUtil.exist(convFilePath)) {
                    log.warn("convert to dest file: {} failed", convFilePath);
                    return;
                }

                audioInfo.setTextStartTime(System.currentTimeMillis());

                String destText = speechRecognitionService.translateSpeechAsync(srcLang, destLang, convFilePath);
                log.info("translated to dest lang: {}, text: {}", destLang, destText);

                audioInfo.setTextEndTime(System.currentTimeMillis());

                log.info("time elapsed for recognition: {} ms", (audioInfo.getTextEndTime() - audioInfo.getTextStartTime()));

                if (StringUtils.isBlank(destText)) {
                    log.warn("No text recognized from file: {}", convFileName);
                    return;
                }

                audioInfo.setDestText(destText);

                audioGenerateService.generateAudio(audioInfo);
                processed = true;
            } finally {
                if(!processed) {
                    audioPlayService.playAudio(audioInfo);
                }
            }
        });

    }
}
