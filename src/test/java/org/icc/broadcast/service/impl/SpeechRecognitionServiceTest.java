package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
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

    @Test
    public void testTts() throws InterruptedException {
        String lang = "my-MM";
        String voiceName = "my-MM-NilarNeural";
        String text = "ဒီနေ့ဟာ လမ်းလျှောက်ဖို့ အလွန်ကောင်းတဲ့ နေ့တစ်နေ့ပါ။";
        String destFilePath = "C:\\dev\\trans\\" + System.currentTimeMillis() + ".wav";

        speechRecognitionService.synthesizeTextToSpeechSsml(lang, voiceName, text, destFilePath);
    }

}
