package org.icc.broadcast.service.impl;


import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.dto.AudioTransDto;
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

    public void translateAudio(AudioTransDto audioTrans, String filePath) {
        log.info("start to translate audio: {} to {}", filePath, audioTrans.getDestLang());

        String srcLang = audioTrans.getSrcLang();
        String destLang = audioTrans.getDestLang();

        SINGLE_POOL.execute(() -> {
            String parentDir = FileUtil.getParent(filePath, 1);
            String fileName = FileUtil.getName(filePath);

            String convFileName = "conved_" + fileName;
            String convFilePath = parentDir + "/" + convFileName;

            ffmpegService.convertToWavS16(filePath, convFilePath);

            if(!FileUtil.exist(convFilePath)) {
                log.warn("convert to dest file: {} failed", convFilePath);
                return;
            }

            String destText = speechRecognitionService.translateSpeechAsync(srcLang, destLang, convFilePath);
            log.info("translated to dest lang: {}, text: {}", destLang, destText);

            if(StringUtils.isBlank(destText)) {
                log.warn("No text recognized from file: {}", convFileName);
                return;
            }


            audioGenerateService.generateAudio(audioTrans, fileName, destText);
        });

    }
}
