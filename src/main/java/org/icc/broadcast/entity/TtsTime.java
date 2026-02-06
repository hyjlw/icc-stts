package org.icc.broadcast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TtsTime {

    private long segmentId;
    private String text;
    private long totalTime;

    private ProcessTime translation;
    private ProcessTime synthesize;

}
