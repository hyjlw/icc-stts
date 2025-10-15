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
@Document(collection = "BroadcastMachineConfig")
public class BroadcastMachineConfig {
    @Id
    private ObjectId id;
    private ObjectId machineId;
    private ObjectId sessionId;

    private Boolean deleted;

    private Date updateTime = new Date();
    private Date createAt = new Date();
}