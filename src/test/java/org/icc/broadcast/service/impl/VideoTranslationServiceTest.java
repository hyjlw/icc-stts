package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class VideoTranslationServiceTest {

    @Resource
    private VideoTranslationService videoTranslationService;
    @Resource
    private StreamUploadService streamUploadService;

    @Test
    public void testTranslate() throws InterruptedException {
//        videoTranslationService.translateVideo("C:\\dev\\files\\19.ts");

        TimeUnit.HOURS.sleep(1);

    }

    @Test
    public void testUpdateIndexFile() {
        streamUploadService.updateIndexFile("1.ts", 8.234235424, 0);
    }

}
