package org.icc.broadcast.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.constant.ProcessType;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.dto.SpeechResult;
import org.icc.broadcast.entity.AudioMeta;
import org.icc.broadcast.entity.ProcessTime;
import org.icc.broadcast.utils.ThreadPoolExecutorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
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
    private final BroadcastAudioService broadcastAudioService;

    public void generateAudio(AudioInfo audioInfo) {
        log.info("start to generate audio: {}", audioInfo);

        SYNTH_POOL.execute(() -> {
            try {
                String destLang = audioInfo.getDestLang();
                String audioModel = audioInfo.getDestModel();
                String sessionId = audioInfo.getSessionId();
                String text = audioInfo.getTranslatedText();
                String subPath = sessionId + "_" + DateUtil.formatDate(new Date());

                String fileName = "voice_" + System.currentTimeMillis() + ".wav";

                String destFilePath = this.transPath + "/" + subPath + "/" + fileName;
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

                log.info("synthesize to audio file: {}", destFilePath);

                long startTime = System.currentTimeMillis();
                SpeechResult speechResult = speechRecognitionService.synthesizeTextToSpeechSsml(destLang, audioModel, text, destFilePath);
                if(speechResult == null) {
                    log.warn("No speech result in speech synthesize");
                    speechResult = SpeechResult.builder()
                            .success(false)
                            .startTime(startTime)
                            .endTime(System.currentTimeMillis())
                            .text("")
                            .build();
                }
                log.info("synthesis status:{}, time cost: {}",
                        speechResult.isSuccess(), (speechResult.getEndTime() - speechResult.getStartTime()));

                if(!speechResult.isSuccess()) {
                    log.warn("synthesize audio is not success");

                    return;
                }

                if (!FileUtil.exist(destFilePath)) {
                    log.warn("generate audio dest: {} file: {} failed", destLang, destFilePath);
                    return;
                }

                audioInfo.setFilePath(destFilePath);
                audioInfo.setFinalFilePath(destFilePath);

                // set dest duration first;
                long destDuration = ffmpegService.getDuration(destFilePath);

                /**
                String destStereoFilePath = this.transPath + "/" + subPath + "/" + "stereo_" + fileName;
                ffmpegService.convertToStereo(destFilePath, destStereoFilePath);

                if (!FileUtil.exist(destStereoFilePath)) {
                    log.warn("generate stereo audio dest: {} file: {} not found, wait and check again", destLang, destStereoFilePath);
                    TimeUnit.MILLISECONDS.sleep(1500);

                    if (!FileUtil.exist(destStereoFilePath)) {
                        log.warn("generate stereo audio dest: {} file: {} failed", destLang, destStereoFilePath);
                        return;
                    }
                }
                audioInfo.setFinalFilePath(destStereoFilePath);
                 */

                AudioMeta audioMeta = AudioMeta.builder()
                        .provider("AZURE")
                        .lang(audioInfo.getDestLang())
                        .audioModel(audioInfo.getDestModel())
                        .duration(destDuration)
                        .filePath(audioInfo.getFilePath().replace(this.transPath, ""))
                        .finalFilePath(audioInfo.getFinalFilePath().replace(this.transPath, ""))
                        .text(audioInfo.getTranslatedText())
                        .build();

                audioInfo.getAudioMetas().add(audioMeta);

                ProcessTime time = ProcessTime.builder()
                        .type(ProcessType.SYNTHESISE.getCode())
                        .startTime(new Date(speechResult.getStartTime()))
                        .endTime(new Date(speechResult.getEndTime()))
                        .duration(speechResult.getEndTime() - speechResult.getStartTime())
                        .errMsg(speechResult.getErrMsg())
                        .build();

                audioInfo.getTimes().add(time);

                // play the audio
                audioPlayService.playAudio(audioInfo);
            } catch (Exception e) {
                log.error("generate final audio error", e);
            } finally {
                // save audio info
                broadcastAudioService.saveAudioInfo(audioInfo);
            }
        });
    }
}
