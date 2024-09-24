package org.icc.broadcast.service.impl;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.icc.broadcast.entity.FtpInfo;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
@Service
@Slf4j
public class FTPClientService {

    public List<String> listFiles(FTPClient ftpClient, String ftpDir) {
        List<String> files = new ArrayList<>();

        try {
            String encFtpDir = new String((ftpClient.printWorkingDirectory() + ftpDir).getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            FTPFile[] ftpFiles = ftpClient.listFiles(encFtpDir);
            if (ftpFiles == null || ftpFiles.length == 0) {
                log.warn("No file exists in remote working dir: {}",  ftpDir);
                return files;
            }

            Arrays.sort(ftpFiles, Comparator.comparing(FTPFile::getTimestampInstant));

            List<FTPFile> ftpFileList = Arrays.stream(ftpFiles).filter(x -> x.getName().endsWith(".ts")).collect(Collectors.toList());

            int size = ftpFileList.size();
            int i = 0;
            for(FTPFile ftpFile : ftpFileList) {
                if(i >= size - 1) {
                    break;
                }
                files.add(ftpFile.getName());
                i++;
            }

        } catch (Exception ex) {
            log.error("FTP listFiles Error: {}", ex.getMessage(), ex);
        }

        return files;
    }


    /**
     * download file from ftp, and save to local disk
     *
     * @param ftpClient       ftp info
     * @param ftpFilePath   ftp file path, e.g. raw/2020/xyz.mp3
     * @param localFilePath local file full path, e.g. /home/ec2-user/data/xyz.mp3
     */
    public boolean download(FTPClient ftpClient, String ftpFilePath, String localFilePath) {
        try {
            String fileName = this.getFileName(ftpFilePath);

            int endIndex = ftpFilePath.lastIndexOf("/");
            String workingDir = ftpFilePath.substring(0, endIndex);

            String ftpWorkingDir = ftpClient.printWorkingDirectory();
            if(!ftpWorkingDir.endsWith(workingDir)) {
                boolean changed = ftpClient.changeWorkingDirectory(workingDir);
                if (!changed) {
                    log.info("ftp not changed to working dir: " + workingDir);
                    return false;
                }
            }
            log.info("ftp client changed working dir to: " + workingDir);

            try {
                Path path = Paths.get(localFilePath);
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
            } catch (Exception e) {
                log.error("create local file path: {}, error: {}", localFilePath, e.getMessage());
                return false;
            }

            File localFile = new File(localFilePath);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));

            log.info("Start pulling file to: " + localFilePath);

            String workingDirFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            FTPFile[] ftpFiles = ftpClient.listFiles(workingDirFileName);
            if (ftpFiles == null || ftpFiles.length == 0) {
                log.warn("No file: {} exists in remote working dir: {}", fileName, workingDir);
                return false;
            }

            FTPFile ftpFile = ftpFiles[0];
            long totalSize = ftpFile.getSize();

            InputStream in = ftpClient.retrieveFileStream(workingDirFileName);

            int chunkSize = 10485760; // 10MB
            if(chunkSize > totalSize) {
                chunkSize = (int) (totalSize / 10);
            }
            byte[] bytesIn = new byte[chunkSize];
            int readBytes;

            while ((readBytes = in.read(bytesIn)) != -1) {
                out.write(bytesIn, 0, readBytes);
            }
            in.close();
            out.close();

            boolean completed = ftpClient.completePendingCommand();
            if (completed) {
                log.info("The file: {} is pulled successfully.", ftpFilePath);
            }

            return completed;
        } catch (Exception ex) {
            log.error("FTP download file Error: {}", ex.getMessage(), ex);
        }

        return false;
    }

    public boolean upload(FTPClient ftpClient, String localFilePath, String ftpFilePath) {
        try {
            int endIndex = ftpFilePath.lastIndexOf("/");
            String workingDir = null;
            if(endIndex > 0) {
                workingDir = ftpFilePath.substring(0, endIndex);
            } else {
                workingDir = ftpFilePath;
            }
            log.info("ftp working dir: " + workingDir);

            String ftpWorkingDir = ftpClient.printWorkingDirectory();
            if(!ftpWorkingDir.endsWith(workingDir)) {
                boolean changed = ftpClient.changeWorkingDirectory(workingDir);
                if (!changed) {
                    log.info("ftp not changed to working dir: " + workingDir);
                    return false;
                }
            }

            File localFile = new File(localFilePath);
            if (!localFile.exists()) {
                log.warn("file not exists: {}", localFilePath);
                return false;
            }

            InputStream inputStream = new FileInputStream(localFile);
            String fileName = this.getFileName(localFilePath);

            String remoteFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            OutputStream outputStream = ftpClient.storeFileStream(remoteFileName);
            byte[] bytesIn = new byte[1024 * 1024 * 4];
            int readBytes;

            while ((readBytes = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, readBytes);
            }
            inputStream.close();
            outputStream.close();

            boolean completed = ftpClient.completePendingCommand();

            return completed;
        } catch (Exception ex) {
            log.error("FTP upload file[" + localFilePath + "] Error: " + ex.getMessage(), ex);
        }

        return false;
    }

    /**
     * utility to create an arbitrary directory hierarchy on the remote ftp server
     *
     * @param client
     * @param dirTree the directory tree only delimited with / chars.  No file name!
     * @throws Exception
     */
    public void ftpCreateAndChangeDirectoryTree(FTPClient client, String dirTree) throws IOException {
        boolean dirExists = true;

        //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
        String[] directories = dirTree.split("/");
        for (String dir : directories) {
            if (!StringUtils.isBlank(dir)) {
                dir = new String(dir.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
                if (dirExists) {
                    dirExists = client.changeWorkingDirectory(dir);
                }
                if (!dirExists) {
                    if (!client.makeDirectory(dir)) {
                        throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + client.getReplyString() + "'");
                    }
                    if (!client.changeWorkingDirectory(dir)) {
                        throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString() + "'");
                    }
                }
            }
        }
    }


    public boolean rename(FtpInfo ftpInfo, String fromName, String toName) {
        FTPClient ftpClient = buildClient(ftpInfo);
        if(ftpClient == null) {
            log.warn("Cannot open connection to ftp server: {}", ftpInfo.host);
            return false;
        }

        boolean success = false;
        try {
            String remoteOldFileName = new String(fromName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            String remoteNewFileName = new String(toName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

            success = ftpClient.rename(remoteOldFileName, remoteNewFileName);
        } catch (Exception ex) {
            log.error("FTP rename file[" + fromName + "] Error: " + ex.getMessage(), ex);
        } finally {
            closeFtpClient(ftpClient);
        }

        return success;
    }

    public boolean delete(FtpInfo ftpInfo, String fileName) {
        FTPClient ftpClient = buildClient(ftpInfo);
        if(ftpClient == null) {
            log.warn("Cannot open connection to ftp server: {}", ftpInfo.host);
            return false;
        }

        boolean success = false;
        try {
            String remoteFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            success = ftpClient.deleteFile(remoteFileName);
            log.info("remove file: " + remoteFileName + ", status" + success);
        } catch (Exception ex) {
            log.error("FTP rename file[" + fileName + "] Error: " + ex.getMessage(), ex);
        } finally {
            closeFtpClient(ftpClient);
        }

        return success;
    }

    private String getFileName(String fileName) {
//        int index = fileName.lastIndexOf("/");
//        if (index > -1) {
//            fileName = fileName.substring(index + 1);
//        }
//
//        return fileName;

        return FileUtil.getName(fileName);
    }

    public FTPClient buildClient(FtpInfo ftpInfo) {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(ftpInfo.host, ftpInfo.port);

            String localCharSet = "GBK";
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                localCharSet = "UTF-8";
            }

            if (!ftpClient.login(ftpInfo.username, ftpInfo.password)) {
                log.warn("ftp user or password not correct");
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
