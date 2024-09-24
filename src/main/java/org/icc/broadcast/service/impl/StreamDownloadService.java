package org.icc.broadcast.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.icc.broadcast.entity.FtpInfo;
import org.icc.broadcast.repo.FtpInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.icc.broadcast.constant.Constants.STREAM_VIDEO_FTP_SERVER;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamDownloadService {


    private static final Cache<String, Boolean> FILE_CACHE = Caffeine.newBuilder().expireAfterWrite(3, TimeUnit.HOURS).build();

    @Value("${file.download.dir}")
    private String baseDir;
    @Value("${file.video.dir}")
    private String videoDir;

    private final FtpInfoRepository ftpInfoRepository;
    private final FTPClientService ftpClientService;
    private final VideoTranslationService videoTranslationService;

    private FTPClient ftpClient;

    private volatile boolean flag = false;

    private static final int BATCH_SIZE = 2;

    private Thread thread;

    public List<String> listStreamFiles() {
        FtpInfo ftpInfo = ftpInfoRepository.findOneBy(Criteria.where("ftpName").is(STREAM_VIDEO_FTP_SERVER));

        FTPClient ftpClient = buildClient(ftpInfo);
        if (ftpClient == null) {
            return null;
        }

        List<String> files = ftpClientService.listFiles(ftpClient, ftpInfo.baseDir);

        return files;
    }

    public void startupVideoTrans() {
        this.flag = true;

        thread = new Thread(this::downloadStreamFiles);
        thread.start();
    }

    public void shutdownVideoTrans() {
        this.flag = false;

        if(thread != null) {
            thread.interrupt();

            thread = null;
        }
    }

    public void downloadStreamFiles() {
        List<File> files = this.listFiles();
        if(CollectionUtil.isEmpty(files)) {

            return;
        }

        int i = 0;
        List<String> filePaths = new ArrayList<>();

        for(File curFile : files) {
            String fileName = curFile.getName();
            log.info("start to process file: {}", fileName);

            if (FILE_CACHE.getIfPresent(fileName) != null) {
                continue;
            }

            filePaths.add(curFile.getPath());
            FILE_CACHE.put(fileName, true);
            i++;

            if(i % BATCH_SIZE == 0) {
                // start to translate
                videoTranslationService.translateVideo(filePaths);

                filePaths.clear();
            }
        }

        if(!CollectionUtil.isEmpty(filePaths)) {
            // start to translate
            videoTranslationService.translateVideo(filePaths);

            filePaths.clear();
        }
    }

    private List<File> listFiles() {
        log.info("list file from: {}", this.videoDir);
        File videoFolder =  new File(this.videoDir);

        File[] files = videoFolder.listFiles();

        if(files == null || files.length == 0) {
            return new ArrayList<>();
        }

        Arrays.sort(files, Comparator.comparing(File::lastModified));

        List<File> fileList = Arrays.stream(files).filter(x -> x.getName().endsWith(".ts")).collect(Collectors.toList());

        fileList.remove(fileList.size() - 1);

        log.info("list files: {}", fileList.size());
        return fileList;
    }

    public FTPClient buildClient(FtpInfo ftpInfo) {
        if(this.ftpClient != null) {
            return this.ftpClient;
        }

        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpInfo.host, ftpInfo.port);

            String localCharSet = "GBK";
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                localCharSet = "UTF-8";
            }

            if (!ftpClient.login(ftpInfo.username, ftpInfo.password)) {
                log.warn("ftp user or password not correct");

                ftpClient.disconnect();
                ftpClient = null;

                return null;
            }

            ftpClient.setControlEncoding(localCharSet);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setAutodetectUTF8(true);

            return ftpClient;
        } catch (Exception e) {
            log.error("build ftp client error: {}", e.getMessage());
        }

        return null;
    }

    private void closeFtpClient() {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }

            ftpClient = null;
        } catch (Exception ex) {
            log.error("FTP close client Error: " + ex.getMessage(), ex);
        }
    }
}
