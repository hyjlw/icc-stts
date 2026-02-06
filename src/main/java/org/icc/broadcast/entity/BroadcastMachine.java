package org.icc.broadcast.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "BroadcastMachine")
public class BroadcastMachine {
    @Id
    private ObjectId id;

    private String key;
    private String name;
    private String ip;
    private String port;
    private String tag;
    private Boolean active;

    private Boolean started;

    private Boolean deleted;

    private Date updateTime = new Date();
    private Date createAt = new Date();
}