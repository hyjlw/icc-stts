package org.icc.broadcast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "google.config")
@RefreshScope
public class GoogCloudConfig {

    private String apiKey;
    private Map<String, String> gtLangMap = new HashMap<>();

}