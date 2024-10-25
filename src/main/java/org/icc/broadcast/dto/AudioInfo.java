package org.icc.broadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioInfo {

    private String sessionId;
    private String srcLang;
    private String destLang;
    private String destModel;

    private String rawFilePath;
    private String rawText;

    /**
     * unit is millisecond
     */
    private long rawDuration;

    /**
     * the audio has been processed
     */
    private boolean processed;
    /**
     * the audio has translated and generated
     */
    private boolean generated;

    private String destFilePath;
    private String destText;

    /**
     * unit is millisecond
     */
    private long destDuration;

    private long textStartTime;
    private long textEndTime;

    private long synthStartTime;
    private long synthEndTime;
}
