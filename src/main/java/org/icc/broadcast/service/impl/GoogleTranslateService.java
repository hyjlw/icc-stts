package org.icc.broadcast.service.impl;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.config.GoogCloudConfig;
import org.icc.broadcast.dto.SpeechResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleTranslateService {

    @Getter
    private String provider = "google";

    private final GoogCloudConfig googCloudConfig;

    private Translate translate;

    @PostConstruct
    public void init() {
        translate = TranslateOptions.newBuilder().setApiKey(googCloudConfig.getApiKey()).build().getService();
    }

    public SpeechResult translateText(String srcLang, String targetLang, String text) {
        log.info("start to google translate chat msg: {}->{}, {}", srcLang, targetLang, text);

        String errMsg = "success";
        SpeechResult speechResult = SpeechResult.builder()
                .srcLang("")
                .startTime(System.currentTimeMillis())
                .text("")
                .errMsg("")
                .success(false)
                .build();

        try {
            srcLang = this.googCloudConfig.getGtLangMap().getOrDefault(srcLang, srcLang);
            targetLang = this.googCloudConfig.getGtLangMap().getOrDefault(targetLang, targetLang);

            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(srcLang),
                    Translate.TranslateOption.targetLanguage(targetLang));


            String translatedText = translation.getTranslatedText();
            log.info("start to google translate msg: {}, {} -> {}, {}", srcLang, text, targetLang, translatedText);

            if(!StringUtils.isBlank(translatedText)) {
                translatedText = translatedText.replaceAll("&#39;", "'")
                        .replaceAll("&#34;", "\"")
                        .replaceAll("&quot;", "\"");

                speechResult.setText(translatedText);
                speechResult.setSuccess(true);
            } else {
                errMsg = "No translated content from google";
            }
        } catch (Exception e) {
            log.error("translate from src: {} to dest: {} error", srcLang, targetLang, e);

            errMsg = e.getMessage();
        } finally {
            speechResult.setErrMsg(errMsg);
            speechResult.setEndTime(System.currentTimeMillis());
        }

        return speechResult;
    }

}
