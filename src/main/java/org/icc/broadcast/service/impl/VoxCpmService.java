package org.icc.broadcast.service.impl;

import cn.hutool.core.io.FileUtil;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.config.VoxCpmConfig;
import org.icc.broadcast.dto.SpeechResult;
import org.icc.broadcast.dto.vox_cpm.TtsReq;
import org.icc.broadcast.dto.vox_cpm.TtsResp;
import org.icc.broadcast.exception.BizException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class VoxCpmService {

    private final VoxCpmConfig voxCpmConfig;

    private final RestTemplate restTemplate;
    private final FileService fileService;
    private final FfmpegService ffmpegService;

    /**
     *
     * @param lang e.g zh, en, my, in
     * @param voiceName e.g wangyi, kongfang
     * @param text
     * @return the file path
     */
    public SpeechResult synthesizeTextToSpeechSsml(String lang, String voiceName, String text, String destFilePath) {
        log.info("start to synthesize text to speech, lang: {}, voiceName: {}, text: {}, destFilePath: {}", lang, voiceName, text, destFilePath);

        SpeechResult speechResult = SpeechResult.builder()
                .srcLang(lang)
                .text("")
                .startTime(System.currentTimeMillis())
                .endTime(System.currentTimeMillis())
                .errMsg("")
                .success(false)
                .build();

        String errMsg = "success";

        try {
            TtsResp ttsResp = this.textToSpeech(lang, voiceName, text);
            if(ttsResp == null || StringUtils.isBlank(ttsResp.getAudioPath())) {
                log.warn("No tts response returned");
                throw new BizException("No tts response returned");
            }

            String audioPath = ttsResp.getAudioPath();
            String baseDir = FileUtil.getParent(destFilePath, 1);
            String rawFilePath = fileService.downloadFile(baseDir, voxCpmConfig.getDownloadUrl() + "?file_path=" + audioPath);
            log.info("download raw file: {}", rawFilePath);

            ffmpegService.convertToWavS16(rawFilePath, destFilePath);
            speechResult.setSuccess(true);
        } catch (Exception e) {
            log.error("text to speech, error: {}", e.getMessage(), e);
            errMsg = e.getMessage();
        } finally {
            speechResult.setEndTime(System.currentTimeMillis());
            speechResult.setErrMsg(errMsg);
        }

        return speechResult;
    }


    public TtsResp textToSpeech(String lang, String refAudioName, String text) {
       log.info("textToSpeech, lang: {}, refAudioName: {}, text: {}", lang, refAudioName, text);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            TtsReq ttsReq = TtsReq.builder()
                    .text(text)
                    .language(lang)
                    .refAudioName(refAudioName)
                    .build();

            HttpEntity<TtsReq> entity = new HttpEntity<>(ttsReq, headers);
            String url = voxCpmConfig.getTtsUrl();

            ResponseEntity<TtsResp> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TtsResp.class
            );
            log.info("textToSpeech, response: {}", response);

            return response.getBody();
        } catch (Exception e) {
            log.error("text to speech, error: {}", e.getMessage(), e);
            throw e;
        }
    }
}