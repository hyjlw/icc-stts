package org.icc.broadcast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "BroadcastAudio")
public class BroadcastAudio {
    @Id
    private ObjectId id;

    private String broadcastId;

    private String sessionId;
    private String srcLang;

    private String rawFilePath;
    private String rawText;
    /**
     * unit is millisecond
     */
    private long rawDuration;

    private List<AudioMeta> audioMetas;
    private List<ProcessTime> times;

    private Date updateTime = new Date();
    private Date createAt = new Date();
}