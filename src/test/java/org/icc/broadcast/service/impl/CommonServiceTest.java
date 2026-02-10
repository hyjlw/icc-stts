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
public class CommonServiceTest {

    @Autowired
    private MachineCommonService machineCommonService;

    @Test
    public void testGeminiTranslate() throws InterruptedException {
        String ip = machineCommonService.getIp();

        log.info("ip: {}", ip);
    }

}
