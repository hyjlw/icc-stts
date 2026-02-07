package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.SpeechResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GeminiServiceTest {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private GoogleTranslateService googleTranslateService;

    @Test
    public void testGeminiTranslate() throws InterruptedException {
        SpeechResult result = geminiService.translateText("zh-CN", "en-US", "之所以能认识真理，是因为我赐给了你光。你若要看清这个世界，必须完全地依靠上帝，如此你便能看得愈发清晰。我是羊的门，我是好牧人，我是来保护你们的。");

        log.info("gemini result: {}", result);
    }

    @Test
    public void testGoogleTranslate() throws InterruptedException {
        SpeechResult result = googleTranslateService.translateText("zh-CN", "my-MM", "之所以能认识真理，是因为我赐给了你光。你若要看清这个世界，必须完全地依靠上帝，如此你便能看得愈发清晰。我是羊的门，我是好牧人，我是来保护你们的。");

        log.info("google result: {}", result);
    }

}
