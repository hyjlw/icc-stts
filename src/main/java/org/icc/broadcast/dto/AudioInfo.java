package org.icc.broadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.icc.broadcast.entity.AudioMeta;
import org.icc.broadcast.entity.ProcessTime;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioInfo {

    private Long serialId;
    private Long segmentId;
    private String srcLang;
    private String broadcastId;
    private String sessionId;
    private String destLang;
    private String destModel;
    private String provider;

    private String rawText;
    private String translatedText;

    private long timestamp;

    private String filePath;
    private String finalFilePath;

    private List<AudioMeta> audioMetas;
    private List<ProcessTime> times;

}
