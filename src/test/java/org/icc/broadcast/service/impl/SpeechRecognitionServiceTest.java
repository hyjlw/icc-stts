package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.SpeechResult;
import org.icc.broadcast.utils.ThreadPoolExecutorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpeechRecognitionServiceTest {

    @Resource
    private SpeechRecognitionService speechRecognitionService;
    @Resource
    private FfmpegService ffmpegService;

    @Resource
    private VoxCpmService voxCpmService;

    @Test
    public void testTts() throws InterruptedException {
        String lang = "my-MM";
        String voiceName = "my-MM-NilarNeural";
        String text = "ဒီနေ့ဟာ လမ်းလျှောက်ဖို့ အလွန်ကောင်းတဲ့ နေ့တစ်နေ့ပါ။";
        String destFilePath = "C:\\dev\\trans\\" + System.currentTimeMillis() + ".wav";

        speechRecognitionService.synthesizeTextToSpeechSsml(lang, voiceName, text, destFilePath);
    }

    @Test
    public void testRecognize() {
        String filePath = "C:\\dev\\files\\142535635\\voice_1759581216190.wav";
        String destFilePath = "C:\\dev\\files\\142535635\\voice_1759581216190_mono.wav";

        ffmpegService.convertToWavS16(filePath, destFilePath);

        String translatedText = speechRecognitionService.translateSpeechAsync("zh-CN", "my-MM", destFilePath);
        log.info("text: {}", translatedText);
    }

    @Test
    public void testText2Speech() {
        SpeechResult speechResult = voxCpmService.synthesizeTextToSpeechSsml("en",
                "wangyi",
                "Before the war began on Feb 28, the narrow waterway carried one-fifth of the world's oil and liquefied natural gas, and has since become a central pressure point in the conflict.",
                "C:\\dev\\trans\\test\\" + System.currentTimeMillis() + ".wav");
        log.info("speechResult: {}", speechResult);
    }
}
