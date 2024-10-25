package org.icc.broadcast.config;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
public class SttsConfig {

    private boolean sttsStarted;

    private Date startTime;
    private Date endTime;

}