package org.icc.broadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushAudioInfo {

    private Long serialId;
    private Long segmentId;
    private String srcLang;
    private String text;
    private Long timestamp;

}
