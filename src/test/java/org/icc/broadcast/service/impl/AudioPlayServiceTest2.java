package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioByteInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AudioPlayServiceTest2 {

    private  DataLine.Info info;
    private  SourceDataLine line;


    @Resource
    private AudioPlayService audioPlayService;

    @Before
    public void before() {
        AudioFormat audioFormat = new AudioFormat(
                16000, // Sample rate (samples per second)
                16,    // Bits per sample
                2,     // Number of channels (1 for mono, 2 for stereo)
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
        String audioPath = "C:\\dev\\trans\\5616515651135\\voice_1759838467455.wav";

        this.doPlayAudio2(audioPath);

        TimeUnit.SECONDS.sleep(100);
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

            int seq = 0;
            long ts = System.currentTimeMillis();
            while ((bytesRead = fis.read(audioBuffer)) != -1) {
                log.info("write seq: {}, data: {}", seq, audioBuffer);
                byte []copiedBytes = Arrays.copyOf(audioBuffer, bytesRead);
            }

        } catch (IOException e) {
            log.error("read audio: {} bytes error", audioPath, e);
        }

    }

    @Test
    public void testPlayBatch() {
//        String []paths = new String[] {
//                "C:\\dev\\trans\\68ed20769502d6b82ad991e9\\stereo_voice_1770215800553.wav",
//                "C:\\dev\\trans\\68ed20769502d6b82ad991e9\\stereo_voice_1770215813853.wav",
//                "C:\\dev\\trans\\68ed20769502d6b82ad991e9\\stereo_voice_1770215821131.wav",
//                "C:\\dev\\trans\\68ed20769502d6b82ad991e9\\stereo_voice_1770215832057.wav",
//                "C:\\dev\\trans\\68ed20769502d6b82ad991e9\\stereo_voice_1770215843982.wav"
//        };
        String []paths = new String[] {
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354695833.wav",
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354705937.wav",
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354713973.wav",
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354722295.wav",
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354727927.wav",
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354732834.wav",
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354746341.wav",
                "C:\\dev\\trans\\68ed1fc89502d6b82ad991dd_2026-02-06\\stereo_voice_1770354751443.wav",
        };

        List<AudioByteInfo> list = new ArrayList<>();

        for(String mp3File : paths) {
            try (FileInputStream fis = new FileInputStream(mp3File)) {
                byte[] audioBuffer = new byte[1280]; // Define a suitable buffer size
                int bytesRead;

                int seq = 0;
                long ts = System.currentTimeMillis();
                while ((bytesRead = fis.read(audioBuffer)) != -1) {
                    byte []copiedBytes = Arrays.copyOf(audioBuffer, bytesRead);

                    if(seq < 2) {
                        for(int i = 0; i < copiedBytes.length; i++) {
                            if(copiedBytes[i] > 1) {
                                copiedBytes[i] = 1;
                            }
                        }
                    }

                    int rest = copiedBytes.length % 4;
                    if(rest > 0) {
                        int len = copiedBytes.length + rest;
                        byte []newBytes = new byte[len];
                        System.arraycopy(copiedBytes, 0, newBytes, 0, copiedBytes.length);

                        for(int i = 0; i < rest; i++) {
                            newBytes[len - i - 1] = 0;
                        }

                        copiedBytes = newBytes;
                    }

                    list.add(AudioByteInfo.builder().seq(seq++).timestamp(ts).bytes(copiedBytes).build());
                }

            } catch (IOException e) {
                log.error("read audio: {} bytes error", mp3File, e);
            }
        }

        this.playAudio(list);
    }

    private void playAudio(List<AudioByteInfo> list) {
        for(AudioByteInfo audioByteInfo : list) {
            byte[] bytes = audioByteInfo.getBytes();

            line.write(bytes, 0, bytes.length);
        }
    }
}
