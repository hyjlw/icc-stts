package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.utils.ThreadPoolExecutorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

/**
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingService {
    private static final Executor STREAM_POOL = ThreadPoolExecutorFactory.getSingle(100000);

    @Value("${ffmpeg.path.main}")
    private String ffmpegPath;
    @Value("${video.streaming.url}")
    private String streamingUrl;

    private final FfmpegService ffmpegService;

    /**
     * streaming video
     *
     * @param localFilePath local file full path
     */
    public void streamVideo(String localFilePath) {
        log.info("start to stream with url: {}", streamingUrl);

//        long duration = ffmpegService.getVideoDuration(localFilePath);
//        if(duration <= 0) {
//            duration = 3;
//        }

        String[] commands = new String[]{
                ffmpegPath,
                "-re",
                "-i", localFilePath,
                "-bufsize", "2048k",
                "-maxrate", "5000k",
                "-preset", "medium",
                "-c", "copy",
                "-f", "flv",
                streamingUrl
        };

        STREAM_POOL.execute(() -> {
            streamVideo(commands);
        });

    }


    /**
     * encode the media
     *
     * @param commands ffmpeg commands
     */
    public boolean streamMedia(String[] commands) {
        try {
            Runtime runtime = Runtime.getRuntime();

            Process process = runtime.exec(commands);
            log.info("Process" + process);
            int exitValue = process.waitFor();
            log.info("Started streamMedia merge with exit code: {}", exitValue);
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
                log.info("streamMedia generation ended successfully. \n {}", normalOutputBuffer.toString());
            }

            // read any errors from the command
            StringBuilder errorOutputBuffer = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                errorOutputBuffer.append(line);
                errorOutputBuffer.append("\n");
            }
            if (!errorOutputBuffer.toString().isEmpty()) {
                log.info("streamMedia generation ended with failure. \n {}", errorOutputBuffer.toString());
            }

            return true;
        } catch (Exception e) {
            log.error("mergeAudioVideo error", e);
        }

        return false;
    }


    /**
     * encode the media
     *
     * @param commands ffmpeg commands
     */
    public boolean streamVideo(String[] commands) {
        ProcessBuilder pb = new ProcessBuilder(commands);
        Scanner sc = null;
        try {
            Process p = pb.start();
            sc = new Scanner(p.getErrorStream());

            // Find duration
            Pattern durPattern = Pattern.compile("(?<=Duration: )[^,]*");
            String duration = sc.findWithinHorizon(durPattern, 0);
            if (duration == null)
                log.warn("-----Could not parse duration.");

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date mDate = sdf.parse(duration);
            } catch (Exception e) {
                log.error("cannot parse the current media total time: {}", e.getMessage());
            }

            // Find time as long as possible.
            Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
            String match;

            while (null != (match = sc.findWithinHorizon(timePattern, 0))) {
                log.info("streamed time: {}", match);
            }

        } catch (Exception e) {
            log.error("stream media file error: ", e);
        } finally {
            if (sc != null) {
                sc.close();
            }
        }

        return true;
    }
}
