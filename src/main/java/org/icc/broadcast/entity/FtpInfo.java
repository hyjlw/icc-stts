package org.icc.broadcast.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@ToString
@Document(collection = "FtpInfo")
public class FtpInfo {
    @Id
    public ObjectId id;
    public String ftpName;
    public String host;
    public int port;
    public String username;
    public String password;
    public String baseDir;
    public int chunkSize;
    public Date createDate = new Date();
}
