package org.icc.broadcast.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "BroadcastSession")
public class BroadcastSession {
    @Id
    public ObjectId id;
    public String sessionName;
    public String author;
    public String srcLang;
    public String destLang;
    public String destModel;
    public String gender;
    public Date startTime;
    public Date endTime;
    public Boolean started;
    public String url;
    public Date updateTime;

    public Date createAt = new Date();
}
