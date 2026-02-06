package org.icc.broadcast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gemini.config")
@RefreshScope
public class GeminiConfig {

    private List<String> apiKeys;
    private String modelName;
    private String translatePrompt;

}