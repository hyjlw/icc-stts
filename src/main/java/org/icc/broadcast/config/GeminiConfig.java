package org.icc.broadcast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gemini.config")
@RefreshScope
public class GeminiConfig {

    private String modelName;
    private String translatePrompt;

}