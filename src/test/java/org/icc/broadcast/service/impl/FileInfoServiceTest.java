package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.icc.broadcast.constant.Constants.DEST_LANG;
import static org.icc.broadcast.constant.Constants.SRC_LANG;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class FileInfoServiceTest {

    @Resource
    private SpeechRecognitionService speechRecognitionService;

    @Resource
    private FfmpegService ffmpegService;


    @Test
    public void testExtractAudio() {
        ffmpegService.extractAudio("C:\\Users\\mathew\\Desktop\\-204.ts", "C:\\Users\\mathew\\Desktop\\-204.m4a");
    }

    @Test
    public void testEncodeAudio() {
        ffmpegService.convertToWavU8("C:\\Users\\mathew\\Desktop\\-204.m4a", "C:\\Users\\mathew\\Desktop\\-204-u8.wav");
    }

    @Test
    public void testTranslate() {
        ffmpegService.extractVideo("C:\\Users\\mathew\\Desktop\\-204.ts", "C:\\Users\\mathew\\Desktop\\-204-video.ts");
    }

    @Test
    public void testExtractVideo() {
        String destText = speechRecognitionService.translateSpeechAsync(SRC_LANG, DEST_LANG, "C:\\Users\\mathew\\Desktop\\-204-u8.wav");

        log.info("dest text: {}", destText);
    }

    @Test
    public void testConvertToAAC() {
        ffmpegService.convertAudioToAAC("C:\\dev\\files\\-75-audio-generated.wav", "C:\\dev\\files\\-75-audio-generated.mp3");
    }

    @Test
    public void testMerge() {
        ffmpegService.mergeAudioVideo("C:\\dev\\files\\-75-audio-generated.mp3", "C:\\dev\\files\\-75-video-without-audio.ts", "C:\\dev\\files\\-75-video-merged.ts");
    }

    @Test
    public void testVideoDuration() {
        double duration = ffmpegService.getDuration("C:\\dev\\files\\18.ts");

        log.info("done, duration: {}", duration);
    }

    @Test
    public void testAudioDuration() {
        double duration = ffmpegService.getDuration("C:\\dev\\files\\18-audio-generated.mp3");

        log.info("done, duration: {}", duration);
    }

}
