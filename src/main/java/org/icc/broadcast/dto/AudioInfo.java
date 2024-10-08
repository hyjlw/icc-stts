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
