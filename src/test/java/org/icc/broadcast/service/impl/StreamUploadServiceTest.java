package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class StreamUploadServiceTest {

    @Resource
    private StreamUploadService streamUploadService;

    @Test
    public void testUploadFiles() {
        String []filePaths = new String[] {
                "C:\\dev\\generates\\0.ts",
                "C:\\dev\\generates\\1.ts",
                "C:\\dev\\generates\\2.ts",
                "C:\\dev\\generates\\3.ts",
        };

        for(String filePath: filePaths) {
            streamUploadService.uploadGeneratedFile(filePath);
        }

        log.info("file info");
    }

}
