package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AudioPlayServiceTest2 {

    private  DataLine.Info info;
    private  SourceDataLine line;

    @Before
    public void before() {
        AudioFormat audioFormat = new AudioFormat(
                24000, // Sample rate (samples per second)
                16,    // Bits per sample
                1,     // Number of channels (1 for mono, 2 for stereo)
                true,  // Signed (true for signed PCM, false for unsigned)
                false  // Big endian (true for big endian, false for little endian)
        );

        try {
            info = new DataLine.Info(SourceDataLine.class, audioFormat);
            line = (SourceDataLine) AudioSystem.getLine(info);

            line.open(audioFormat);
            line.start();
        } catch (LineUnavailableException e) {
            log.error("create audio line error", e);

            if(line != null) {
                line.drain(); // Ensures all buffered data is played
                line.stop();
                line.close();
            }
        }
    }

    @After
    public void after() {
        if(line != null) {
            line.drain(); // Ensures all buffered data is played
            line.stop();
            line.close();
        }
    }


    @Test
    public void testPlay() throws InterruptedException {
//        String audioPath = "D:\\dev_space\\GPTDeskWorkspace\\f5-tts\\tests\\api_out_laoxue3.wav";
//        String audioPath = "C:\\dev\\trans\\68440ba26dea1bc04fdacaa6\\voice_1750518796525.wav";
        String audioPath = "C:\\dev\\trans\\1759325221075.wav";

        this.doPlayAudio2(audioPath);
    }

    private void doPlayAudio2(String audioPath) {
        log.info("start to play audio2: {}", audioPath);

        File mp3File = new File(audioPath);

        if (!mp3File.exists()) {
            log.warn("audio file: {} does not exist", audioPath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(mp3File)) {
            byte[] audioBuffer = new byte[1024]; // Define a suitable buffer size
            int bytesRead;

            while ((bytesRead = fis.read(audioBuffer)) != -1) {
                line.write(audioBuffer, 0, bytesRead);
            }

        } catch (IOException e) {
            log.error("read audio: {} bytes error", audioPath, e);
        }

    }
}
