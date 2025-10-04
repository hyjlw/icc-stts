package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class FfmpegServiceTest {

    @Resource
    private FfmpegService ffmpegService;


    @Test
    public void testStretchAudio() {
        String srcPath = "C:\\dev\\trans\\1759325221075.wav";
        String destPath = "C:\\dev\\trans\\1759325221075_"+System.currentTimeMillis()+"_stretch.wav";
        ffmpegService.stretchAudio(srcPath, destPath, 1.1);

        boolean exists = Files.exists(Path.of(destPath));
        log.info("dest file exists: {}", exists);
    }

}
