package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@Slf4j
public class FfmpegService {

    @Value("${ffmpeg.path.main}")
    private String ffmpegPath;
    @Value("${ffmpeg.path.probe}")
    private String ffprobePath;

    /**
     * get the video duration
     * @param filePath video full path
     * @return milliseconds
     */
    public long getDuration(String filePath) {
        try {
            FFprobe ffprobe = new FFprobe(ffprobePath);
            FFmpegProbeResult probeResult = ffprobe.probe(filePath);

            FFmpegFormat format = probeResult.getFormat();
            double duration = format.duration * 1000;

            return Double.valueOf(duration).longValue();
        } catch (Exception e) {
            log.error("get file: {} duration error: {}", filePath, e.getMessage());
        }

        return 0;
    }

    public boolean convertToWavU8(String srcPath, String destPath) {
        try {
            FFmpeg fFmpeg = new FFmpeg(ffmpegPath);

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(srcPath)
                    .overrideOutputFiles(true)
                    .addOutput(destPath)
                    .setFormat("wav")
                    .setAudioCodec("pcm_u8")
                    .setAudioBitRate(22_050).done()
                    ;

            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg);

            executor.createJob(builder).run();

            return true;
        } catch (Exception e) {
            log.error("convertToWav error", e);
        }

        return false;
    }

    public boolean convertToWavS16(String srcPath, String destPath) {
        try {
            FFmpeg fFmpeg = new FFmpeg(ffmpegPath);

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(srcPath)
                    .overrideOutputFiles(true)
                    .addOutput(destPath)
                    .setFormat("wav")
                    .setAudioCodec("pcm_s16le")
                    .setAudioBitRate(44_100).done()
                    ;

            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg);

            executor.createJob(builder).run();

            return true;
        } catch (Exception e) {
            log.error("convertToWav error", e);
        }

        return false;
    }

    public boolean extractVideo(String srcPath, String destPath) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] command = {ffmpegPath,
                    "-i", srcPath, "-vcodec", "copy", "-an", destPath, "-y"
            };
            Process process = runtime.exec(command);
            log.info("Process" + process);
            int exitValue = process.waitFor();
            log.info("Started video with exit code: {}", exitValue);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info("stdInput" + stdInput.readLine());
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            log.info("stdError" + stdError);

            // read the output from the command
            StringBuilder normalOutputBuffer = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                log.info("This is ffmpeg try  while block");
                normalOutputBuffer.append(line);
                if (!line.contains("Done:")) {
                    normalOutputBuffer.append("\n");
                }

            }
            if (!normalOutputBuffer.toString().isEmpty()) {
                log.debug("video generation ended successfully. \n {}", normalOutputBuffer.toString());
            }

            // read any errors from the command
            StringBuilder errorOutputBuffer = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                errorOutputBuffer.append(line);
                errorOutputBuffer.append("\n");
            }
            if (!errorOutputBuffer.toString().isEmpty()) {
                log.debug("video generation ended with failure. \n {}", errorOutputBuffer.toString());
            }

            return true;
        } catch (Exception e) {
            log.error("extractVideo error", e);
        }

        return false;
    }

    public boolean extractAudio(String srcPath, String destPath) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] command = {ffmpegPath,
                    "-i", srcPath, "-acodec", "pcm_u8", destPath, "-y"
            };
            Process process = runtime.exec(command);
            log.info("Process" + process);
            int exitValue = process.waitFor();
            log.info("Started audio extraction with exit code: {}", exitValue);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info("stdInput" + stdInput.readLine());
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            log.info("stdError" + stdError);

            // read the output from the command
            StringBuilder normalOutputBuffer = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                log.info("This is ffmpeg try  while block");
                normalOutputBuffer.append(line);
                if (!line.contains("Done:")) {
                    normalOutputBuffer.append("\n");
                }

            }
            if (!normalOutputBuffer.toString().isEmpty()) {
                log.debug("audio generation ended successfully. \n {}", normalOutputBuffer.toString());
            }

            // read any errors from the command
            StringBuilder errorOutputBuffer = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                errorOutputBuffer.append(line);
                errorOutputBuffer.append("\n");
            }
            if (!errorOutputBuffer.toString().isEmpty()) {
                log.debug("audio generation ended with failure. \n {}", errorOutputBuffer.toString());
            }

            return true;
        } catch (Exception e) {
            log.error("extractAudio error", e);
        }

        return false;
    }

    public boolean convertAudioToAAC(String srcPath, String destPath) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] command = {ffmpegPath,
                    "-i", srcPath, "-ac", "2", "-c:a", "aac", "-ar", "48000", destPath, "-y"
            };
            Process process = runtime.exec(command);
            log.info("Process" + process);
            int exitValue = process.waitFor();
            log.info("Started convertAudioToAAC with exit code: {}", exitValue);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info("stdInput" + stdInput.readLine());
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            log.info("stdError" + stdError);

            // read the output from the command
            StringBuilder normalOutputBuffer = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                log.info("This is ffmpeg try  while block");
                normalOutputBuffer.append(line);
                if (!line.contains("Done:")) {
                    normalOutputBuffer.append("\n");
                }

            }
            if (!normalOutputBuffer.toString().isEmpty()) {
                log.debug("convertAudioToAAC generation ended successfully. \n {}", normalOutputBuffer.toString());
            }

            // read any errors from the command
            StringBuilder errorOutputBuffer = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                errorOutputBuffer.append(line);
                errorOutputBuffer.append("\n");
            }
            if (!errorOutputBuffer.toString().isEmpty()) {
                log.debug("convertAudioToAAC generation ended with failure. \n {}", errorOutputBuffer.toString());
            }

            return true;
        } catch (Exception e) {
            log.error("convertAudioToAAC error", e);
        }

        return false;
    }

    public boolean mergeAudioVideo(String audioPath, String videoPath, String destPath) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] command = {
                    ffmpegPath, "-i", videoPath, "-i", audioPath, "-vcodec", "copy", "-acodec", "copy", destPath, "-y"
            };
            Process process = runtime.exec(command);
            log.info("Process" + process);
            int exitValue = process.waitFor();
            log.info("Started video audio merge with exit code: {}", exitValue);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info("stdInput" + stdInput.readLine());
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            log.info("stdError" + stdError);

            // read the output from the command
            StringBuilder normalOutputBuffer = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                log.info("This is ffmpeg try  while block");
                normalOutputBuffer.append(line);
                if (!line.contains("Done:")) {
                    normalOutputBuffer.append("\n");
                }

            }
            if (!normalOutputBuffer.toString().isEmpty()) {
                log.debug("mergeAudioVideo generation ended successfully. \n {}", normalOutputBuffer.toString());
            }

            // read any errors from the command
            StringBuilder errorOutputBuffer = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                errorOutputBuffer.append(line);
                errorOutputBuffer.append("\n");
            }
            if (!errorOutputBuffer.toString().isEmpty()) {
                log.debug("mergeAudioVideo generation ended with failure. \n {}", errorOutputBuffer.toString());
            }

            return true;
        } catch (Exception e) {
            log.error("mergeAudioVideo error", e);
        }

        return false;
    }

    public boolean stretchAudio(String audioPath, String destPath, double atempo) {
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] command = {
                    ffmpegPath, "-i", audioPath, "-filter:a", "atempo=" + atempo, "-ar", "24000", "-ac", "1", destPath, "-y"
            };
            //, "-shortest", "-af", "apad"
            Process process = runtime.exec(command);
            log.info("[stretchAudio]Process {}", process);
            int exitValue = process.waitFor();
            log.info("[stretchAudio]Started audio stretch with exit code: {}", exitValue);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            log.info("[stretchAudio]stdInput {}", stdInput.readLine());
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            log.info("[stretchAudio]stdError {}", stdError);

            // read the output from the command
            StringBuilder normalOutputBuffer = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                log.info("[stretchAudio]This is ffmpeg try  while block");
                normalOutputBuffer.append(line);
                if (!line.contains("Done:")) {
                    normalOutputBuffer.append("\n");
                }

            }
            if (!normalOutputBuffer.toString().isEmpty()) {
                log.debug("[stretchAudio]stretchAudio generation ended successfully. \n {}", normalOutputBuffer.toString());
            }

            // read any errors from the command
            StringBuilder errorOutputBuffer = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                errorOutputBuffer.append(line);
                errorOutputBuffer.append("\n");
            }
            if (!errorOutputBuffer.toString().isEmpty()) {
                log.debug("[stretchAudio]stretchAudio generation ended with failure. \n {}", errorOutputBuffer.toString());
            }

            return true;
        } catch (Exception e) {
            log.error("[stretchAudio]stretchAudio error", e);
        }

        return false;
    }

}