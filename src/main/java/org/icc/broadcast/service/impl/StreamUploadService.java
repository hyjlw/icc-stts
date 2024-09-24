package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.icc.broadcast.dto.FileDesc;
import org.icc.broadcast.entity.FtpInfo;
import org.icc.broadcast.pool.ThreadPoolExecutorFactory;
import org.icc.broadcast.repo.FtpInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import static org.icc.broadcast.constant.Constants.STREAM_VIDEO_FTP_SERVER;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamUploadService {

    private static final Executor UPLOAD_POOL = ThreadPoolExecutorFactory.getSingle(100000);
    @Value("${file.download.dir}")
    private String baseDir;

    @Value("${file.generated.dir}")
    private String genDir;

    @Value("${file.broadcast.dir}")
    private String broadcastDir;

    @Value("${file.broadcast.index-file}")
    private String indexFile;


    private final FtpInfoRepository ftpInfoRepository;
    private final FTPClientService ftpClientService;

    private FTPClient ftpClient;


    public void updateIndexFile(String fileName, double duration, int index) {
        log.info("update index file: {}, {}, {}", fileName, duration, index);

        String filePath = broadcastDir + "/" + indexFile;
        File file = new File(filePath);
        try {
            Path path = Paths.get(filePath);

            if(!file.exists()) {
                file.createNewFile();

                String str = "#EXTM3U\n" +
                        "#EXT-X-VERSION:3\n" +
                        "#EXT-X-MEDIA-SEQUENCE:0\n" +
                        "#EXT-X-TARGETDURATION:9\n" +
                        "#EXT-X-DISCONTINUITY\n";

                byte[] strToBytes = str.getBytes();

                Files.write(path, strToBytes);
            }

            String content = String.format("#EXTINF:%.3f,\n" + "%s\n", duration, fileName);
            log.info("append content: {}", content);
            Files.write(path, content.getBytes(), StandardOpenOption.APPEND);

            updateIndexSeq2(index, index + 1);
        } catch (IOException e) {
            log.error("update index file error: ", e);
        }
    }

    public void updateIndexFile(List<FileDesc> fileDescList, int startIndex) {
        log.info("update index file: {}, {}", fileDescList, startIndex);

        String filePath = broadcastDir + "/" + indexFile;
        File file = new File(filePath);
        try {
            Path path = Paths.get(filePath);

            if(!file.exists()) {
                file.createNewFile();

                String str = "#EXTM3U\n" +
                        "#EXT-X-VERSION:3\n" +
                        "#EXT-X-MEDIA-SEQUENCE:0\n" +
                        "#EXT-X-TARGETDURATION:9\n" +
                        "#EXT-X-DISCONTINUITY\n";

                byte[] strToBytes = str.getBytes();

                Files.write(path, strToBytes);
            }

            String finalContent = "";
            for(FileDesc fd : fileDescList) {
                String content = String.format("#EXTINF:%.3f,\n" + "%s\n", fd.getDuration(), fd.getFileName());
                log.info("append content: {}", content);

                finalContent += content;
            }

            Files.write(path, finalContent.getBytes(), StandardOpenOption.APPEND);

            updateIndexSeq2(startIndex, startIndex + fileDescList.size());
        } catch (IOException e) {
            log.error("update index file error: ", e);
        }
    }

    private void updateIndexSeq2(int startIndex, int endIndex) {
        String filePath = broadcastDir + "/" + indexFile;

        try {
            String []commandArr = new String[]{
                    "sed",
                    "-i",
                    String.format("s/#EXT-X-MEDIA-SEQUENCE:%d/#EXT-X-MEDIA-SEQUENCE:%d/",
                            startIndex, endIndex),
                    filePath
            };

            log.info("update seq command: {}", Arrays.toString(commandArr));
            Runtime.getRuntime().exec(commandArr);
        } catch (IOException e) {
            log.error("update index seq error: {}", e.getMessage());
        }
    }

    public void uploadGeneratedFile(String filePath) {
        FtpInfo ftpInfo = ftpInfoRepository.findOneBy(Criteria.where("ftpName").is(STREAM_VIDEO_FTP_SERVER));

        FTPClient ftpClient = buildClient(ftpInfo);
        if (ftpClient == null) {
            return;
        }

        UPLOAD_POOL.execute(() -> {
            ftpClientService.upload(ftpClient, filePath, ftpInfo.getBaseDir());
        });
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

    private void closeFtpClient(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception ex) {
            log.error("FTP close client Error: " + ex.getMessage(), ex);
        }
    }
}
