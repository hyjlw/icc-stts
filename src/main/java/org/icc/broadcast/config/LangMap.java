package org.icc.broadcast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "lang")
@RefreshScope
public class LangMap {
    /**
     * en -> en-US
     */
    private Map<String, String> map = new HashMap<>();
    /**
     * en -> English
     */
    private Map<String, String> codeMap = new HashMap<>();
    /**
     * en -> en, zh->zh-Hans
     */
    private Map<String, String> tranMap = new HashMap<>();
}