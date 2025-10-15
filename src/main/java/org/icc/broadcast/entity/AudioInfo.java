package org.icc.broadcast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioInfo {

    private String lang;
    private String audioModel;
    private String generator;
    /**
     * millisecond unit
     */
    private Integer duration;

    private String filePath;

    private String text;
}
