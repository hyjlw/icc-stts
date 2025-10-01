package org.icc.broadcast.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.config.AudioSttsConfig;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.ws.SocketMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioProcessService {
    private final static int WEIGHT = -1;

    private final static BlockingQueue<SocketMsg> MSG_QUEUE = new LinkedBlockingQueue<>(1000000);

    @Value("${audio.save.path}")
    private String audioPath;

    private AudioFormat audioFormat;

    long startMilis = System.currentTimeMillis();
    long curMilis;
    long startSilentMilis = System.currentTimeMillis();
    boolean silent = false;
    boolean saveToFile = false;
    private boolean finished = false;
    private String curAudioPath;
    private long timestamp;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private AudioTransDto audioTrans;

    private final AudioDetermineService audioDetermineService;

    private final AudioSttsConfig audioSttsConfig;

    @PostConstruct
    public void init() {
        audioFormat = getAudioFormat();

        new Thread(this::consume).start();
    }

    public void put(SocketMsg socketMsg) {
        try {
            MSG_QUEUE.put(socketMsg);
        } catch (InterruptedException e) {
            log.error("put socket msg error", e);
        }
    }

    public void consume() {
        for (;;) {
            try {
                SocketMsg socketMsg = MSG_QUEUE.take();

                handleAudioData(socketMsg);
            } catch (InterruptedException e) {
                log.error("process socket msg error: ", e);
            }
        }
    }

    public void startToHandleAudio(AudioTransDto audioTrans) {
        log.info("start to handle audio req: {}", audioTrans);
        this.audioTrans = audioTrans;
        curAudioPath = this.audioPath + "/" + audioTrans.getSessionId();

        if(!FileUtil.exist(curAudioPath)) {
            try {
                log.info("create folder: {}", curAudioPath);
                Files.createDirectories(Path.of(curAudioPath));
            } catch (IOException e) {
                log.error("create dir err: ", e);
            }
        }

        baos.reset();
        startMilis = System.currentTimeMillis();
        startSilentMilis = System.currentTimeMillis();
        silent = false;
        saveToFile = false;
    }

    public void handleAudioData(SocketMsg socketMsg) {
        log.debug("start to hand socketMsg: {}", socketMsg);

        byte[] fragment = socketMsg.getData();
        int fLen = fragment.length;

        if (timestamp == 0) {
            timestamp = socketMsg.getTimestamp();
        }

        ByteArrayInputStream bais = null;
        AudioInputStream ais = null;

        try {
            if (Math.abs(fragment[fLen - 1]) > WEIGHT || baos.size() > 0) {
                baos.write(fragment);

                double volumeRMS = volumeRMS(fragment);
                if (Boolean.TRUE.equals(audioSttsConfig.getShowVolumeLog())) {
                    log.info("current byte data volume rms: {}", volumeRMS);
                }

                if (volumeRMS < audioSttsConfig.getSilentWeight()) {
                    if (!silent) {
                        silent = true;
                        startSilentMilis = System.currentTimeMillis();
                    }
                } else {
                    silent = false;
                }

                if (silent) {
                    curMilis = System.currentTimeMillis();

                    long passedMils = curMilis - startMilis;
                    long silentMils = curMilis - startSilentMilis;
                    if (passedMils > 5000) {
                        if (silentMils > 500) {
                            log.info("5s, 0.s silent, 停止录入");
                            saveToFile = true;
                        }
                    }
                    if (passedMils > 7000) {
                        if (silentMils > 200) {
                            log.info("7s, 0.2s silent, 停止录入");
                            saveToFile = true;
                        }
                    }
                    if (passedMils > 8000) {
                        if (silentMils > 100) {
                            log.info("8s, 0.1s silent, 停止录入");
                            saveToFile = true;
                        }
                    }
                }

                curMilis = System.currentTimeMillis();
                if (curMilis - startMilis > 10000) {
                    log.info("10s停止录入");
                    saveToFile = true;
                }
            }

            if(saveToFile) {
                String filePath = this.curAudioPath + "/voice_" + System.currentTimeMillis() + ".wav";
                File audioFile = new File(filePath);

                byte[] audioData = baos.toByteArray();
                bais = new ByteArrayInputStream(audioData);
                ais = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());

                //定义最终保存的文件名
                log.info("开始生成语音文件: {}", audioFile.getName());
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);

                ais.close();
                bais.close();

                AudioInfo audioInfo = BeanUtil.copyProperties(audioTrans, AudioInfo.class);
                audioInfo.setRawFilePath(filePath);
                audioInfo.setGenerated(false);
                audioInfo.setProcessed(false);
                audioInfo.setTimestamp(timestamp);

                long duration = curMilis - startMilis;
                audioInfo.setRawDuration(duration);
                audioInfo.setDestDuration(duration);

                audioDetermineService.determineAudio(audioInfo);

                // reset timestamp
                this.timestamp = 0;

                baos.reset();
                silent = false;
                startMilis = System.currentTimeMillis();
                saveToFile = false;
            }
        } catch (Exception e) {
            log.error("handle audio data error: ", e);
        } finally {
            if(!finished) {
                return;
            }
            try {
                if(ais != null) {
                    ais.close();
                }
                if(bais != null) {
                    bais.close();
                }

                baos.close();
            } catch (Exception e) {
                log.error("close stream error, ", e);
            }
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        // 8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        // 8,16
        int channels = 2;
        // 1,2
        boolean signed = true;
        // true,false
        boolean bigEndian = false;
        // true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }// end getAudioFormat

    private double volumeRMS(byte[] raw) {
        long sum = 0L;
        if (raw.length==0) {
            return sum;
        } else {
            for (int ii=0; ii<raw.length; ii++) {
                sum += raw[ii];
            }
        }
        double average = (sum * 1.0) / raw.length;

        double sumMeanSquare = 0d;
        for (int ii=0; ii<raw.length; ii++) {
            sumMeanSquare += Math.pow(raw[ii]-average, 2d);
        }
        double averageMeanSquare = sumMeanSquare/raw.length;
        double rootMeanSquare = Math.sqrt(averageMeanSquare);

        return rootMeanSquare;
    }
}
