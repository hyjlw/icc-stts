package org.icc.broadcast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioMeta {

    private String lang;
    private String audioModel;
    private String provider;
    /**
     * millisecond unit
     */
    private Long duration;

    private String filePath;
    private String finalFilePath;

    private String text;

}
