package org.icc.broadcast.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.config.GeminiConfig;
import org.icc.broadcast.config.LangMapConfig;
import org.icc.broadcast.dto.SpeechResult;
import org.icc.broadcast.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("#{'${gemini.config.apiKeys}'.split(',')}")
    private List<String> apiKeys;

    @Getter
    private String provider = "gemini";

    private List<Client> clients = new ArrayList<>();

    private final GeminiConfig geminiConfig;
    private final LangMapConfig langMapConfig;

    @PostConstruct
    public void init() {
        try {
            for(String apiKey : apiKeys) {
                Client client = Client.builder()
                        .apiKey(apiKey.trim())
                        .build();

                clients.add(client);
            }
        } catch (Exception e) {
            log.error("init gemini client error", e);
        }
    }

    public SpeechResult translateText(String srcLang, String destLang, String text) {
        log.debug("start to translate text with gemini from: {} to: {}, text: {}", srcLang, destLang, text);

        String errMsg = "success";
        SpeechResult speechResult = SpeechResult.builder()
                .srcLang("")
                .startTime(System.currentTimeMillis())
                .text("")
                .errMsg("")
                .success(false)
                .build();

        try {
            Client client = getOneClient();

            if(client == null) {
                errMsg = "gemini client is not inited...";
                log.warn(errMsg);

                throw new BizException(errMsg);
            }

            if(StringUtils.isBlank(text)) {
                log.warn("translation text is empty");
                throw new BizException("translation text is empty");
            }

            String srcLangText = langMapConfig.getTranMap().getOrDefault(srcLang, srcLang);
            String destLangText = langMapConfig.getTranMap().getOrDefault(destLang, destLang);

            String prompt = geminiConfig.getTranslatePrompt()
                    .replace("{srcLang}", srcLangText)
                    .replace("{destLang}", destLangText);

            Content sysContent = Content.builder().parts(
                    Part.builder()
                            .text(prompt)
                            .build()
            ).build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(sysContent)
                    .temperature(0.2F)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    geminiConfig.getModelName(),
                    text,
                    config
            );

            String respText = response.text();
            log.debug("response text: {}", respText);

            if(!StringUtils.isBlank(respText)) {
                speechResult.setText(respText);
                speechResult.setSuccess(true);
            } else {
                errMsg = "No translation result from gemini";
            }
        } catch (Exception e) {
            log.error("error in gemini invoke", e);
            errMsg = e.getMessage();
        } finally {
            speechResult.setErrMsg(errMsg);
            speechResult.setEndTime(System.currentTimeMillis());
        }

        return speechResult;
    }


    private Random random = new Random();
    private Client getOneClient() {
        if(CollectionUtil.isEmpty(clients)) {
            return null;
        }

        int idx = random.nextInt(clients.size());

        return clients.get(idx);
    }
}
