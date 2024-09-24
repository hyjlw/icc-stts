package org.icc.broadcast.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressInfo {
    public int duration;
    public double progress;
    public int restSecs;
    public boolean success;
}
