package org.icc.broadcast.service.impl;

import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.constant.ProcessType;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.dto.SpeechResult;
import org.icc.broadcast.entity.ProcessTime;
import org.icc.broadcast.utils.ThreadPoolExecutorFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioTranslationService {

    private static final Executor TRANS_POOL = ThreadPoolExecutorFactory.getSingle(10000);

    private final GeminiService geminiService;

    private final AudioGenerateService audioGenerateService;
    private final BroadcastAudioService broadcastAudioService;

    public void translateAudio(AudioInfo audioInfo) {
        log.info("start to translate audio info: {}", audioInfo);

        TRANS_POOL.execute(() -> {
            boolean processed = false;

            try {
                String srcLang = audioInfo.getSrcLang();
                String destLang = audioInfo.getDestLang();
                String text = audioInfo.getRawText();

                long startTime = System.currentTimeMillis();
                String provider = geminiService.getProvider();

                SpeechResult speechResult = geminiService.translateText(srcLang, destLang, text);

                if(speechResult == null) {
                    log.warn("No translation result from {}", provider);
                    speechResult = SpeechResult.builder()
                            .success(false)
                            .startTime(startTime)
                            .endTime(System.currentTimeMillis())
                            .text("")
                            .build();
                }
                log.info("translate result: {}, time cost: {}", speechResult, (speechResult.getEndTime() - speechResult.getStartTime()));

                audioInfo.setTranslatedText(speechResult.getText());

                audioInfo.getTimes().add(ProcessTime.builder()
                        .type(ProcessType.TRANSLATION.getCode())
                        .startTime(new Date(speechResult.getStartTime()))
                        .endTime(new Date(speechResult.getEndTime()))
                        .duration(speechResult.getEndTime() - speechResult.getStartTime())
                        .errMsg(speechResult.getErrMsg())
                        .build());

                // if not translated, set raw text
                if(!speechResult.isSuccess()) {
                    log.warn("current audio info: {} is not translated, skip audio generation", audioInfo.getSerialId());
                    return;
                }

                audioGenerateService.generateAudio(audioInfo);
                processed = true;
            } catch (Exception e) {
                log.error("translate audio text error", e);
            } finally {
                if(!processed) {
                    broadcastAudioService.saveAudioInfo(audioInfo);
                }
            }
        });

    }
}
