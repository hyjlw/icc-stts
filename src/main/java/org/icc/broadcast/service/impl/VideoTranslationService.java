package org.icc.broadcast.service.impl;


import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.dto.FileDesc;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

import static org.icc.broadcast.constant.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoTranslationService {

    private static final Executor SINGLE_POOL = ThreadPoolExecutorFactory.getSingle(100000);

    @Value("${file.generated.dir}")
    private String genDir;
    @Value("${file.broadcast.dir}")
    private String broadcastDir;

    private final FfmpegService ffmpegService;
    private final SpeechRecognitionService speechRecognitionService;
    private final StreamUploadService streamUploadService;

    private static int index = 0;

    public void translateVideo(List<String> filePaths) {
        int size = filePaths.size();

        ExecutorService executor = Executors.newFixedThreadPool(size);
        //创建CompletionService
        CompletionService<FileDesc> cs = new ExecutorCompletionService<>(executor);

        for(String filePath : filePaths) {

            cs.submit(() -> {
                String fileName = FileUtil.getName(filePath);
                String fileDir = genDir;
                String fileNameWithoutEx = fileName.substring(0, fileName.lastIndexOf("."));
                String fileEx = fileName.substring(fileName.lastIndexOf(".") + 1);

                String videoWithoutAudioFileName = fileNameWithoutEx + "-video-without-audio." + fileEx;
                String audioFileNamePcmU8 = fileNameWithoutEx + "-audio-pcm-u8.wav";
                String audioFileNameTextToSpeech = fileNameWithoutEx + "-audio-generated.wav";
                String audioFileNameTextToSpeechAAC = fileNameWithoutEx + "-audio-generated.m4a";
                String audioStretchedFileNameTextToSpeechAAC = fileNameWithoutEx + "-audio-stretched.m4a";

                String videoWithoutAudioFilePath = fileDir + "/" + videoWithoutAudioFileName;
                String audioFilePathPcmU8 = fileDir + "/" + audioFileNamePcmU8;
                String audioFilePathTextToSpeech = fileDir + "/" + audioFileNameTextToSpeech;
                String audioFilePathTextToSpeechAAC = fileDir + "/" + audioFileNameTextToSpeechAAC;
                String audioStretchedFilePathTextToSpeechAAC = fileDir + "/" + audioStretchedFileNameTextToSpeechAAC;
                String videoMergedFilePath = broadcastDir + "/" + fileName;

    //        SINGLE_POOL.execute(() -> {
                // extract audio and video
                ffmpegService.extractVideo(filePath, videoWithoutAudioFilePath);
                ffmpegService.extractAudio(filePath, audioFilePathPcmU8);
                // translate to direct lang
                String destText = speechRecognitionService.translateSpeechAsync(SRC_LANG, DEST_LANG, audioFilePathPcmU8);
                log.info("translated to dest lang: {}, text: {}", DEST_LANG, destText);

                double videoDuration = ffmpegService.getDuration(filePath);

                if (!StringUtils.isBlank(destText)) {
                    // text to speech
                    speechRecognitionService.synthesizeTextToSpeech(TEXT_TO_SPEECH_LANG, TEXT_TO_SPEECH_MODEL, destText, audioFilePathTextToSpeech);
                    //convert to aac
                    ffmpegService.convertAudioToAAC(audioFilePathTextToSpeech, audioFilePathTextToSpeechAAC);
                    //stretch audio
                    double audioDuration = ffmpegService.getDuration(audioFilePathTextToSpeechAAC);

                    double atempo = 1.0;
                    if (audioDuration > videoDuration) {
                        atempo = audioDuration / videoDuration;
                    }
                    log.info("stretch audio {} / {} = {}", audioDuration, videoDuration, atempo);

                    if (atempo > 2) {
                        atempo = 2.0;
                    }

                    ffmpegService.stretchAudio(audioFilePathTextToSpeechAAC, audioStretchedFilePathTextToSpeechAAC, atempo);
                    // merge video and direct lang audio
                    ffmpegService.mergeAudioVideo(audioStretchedFilePathTextToSpeechAAC, videoWithoutAudioFilePath, videoMergedFilePath);
                } else {
                    // copy source file to dest dir
                    FileUtil.copy(filePath, videoMergedFilePath, true);
                }

                return FileDesc.builder().fileName(fileName).duration(videoDuration).build();
            });
        }

        List<FileDesc> fileDescs = new ArrayList<>();
        try {
            for(int i = 0; i < size; i++) {
                FileDesc fd = cs.take().get();

                fileDescs.add(fd);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("exception: {}", e.getMessage(), e);
        }

        // update index.m3u8

        fileDescs.sort(Comparator.comparing(FileDesc::getFileName));

        streamUploadService.updateIndexFile(fileDescs, index);
        index += fileDescs.size();

            executor.shutdown();

//        });

    }

    public void updateMediaIndex() {
        List<FileDesc> fileDescList = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            fileDescList.add(FileDesc.builder().fileName((index + i) + ".ts").duration(9.0).build());
        }

        streamUploadService.updateIndexFile(fileDescList, index);

        index += fileDescList.size();
    }

    public void resetIndex() {
        index = 0;

        try {
            Runtime.getRuntime().exec(new String[]{
                    "rm",
                    "-rf",
                    this.broadcastDir + "/*"
            });
        } catch (IOException e) {
            log.error("rm broadcast dir error: ", e);
        }
    }
}
