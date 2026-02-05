package org.icc.broadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpeechResult {

    private String srcLang;
    private String text;
    private String errMsg;

    private long startTime;
    private long endTime;

    private boolean success;

}
