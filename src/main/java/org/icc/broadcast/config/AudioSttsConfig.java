package org.icc.broadcast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "audio.stts")
@RefreshScope
public class AudioSttsConfig {

    private Double silentWeight = 20.0;
    private Boolean showVolumeLog = false;

}