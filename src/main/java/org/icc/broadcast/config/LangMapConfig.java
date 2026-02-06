package org.icc.broadcast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "lang.map")
@RefreshScope
public class LangMapConfig {

    private Map<String, String> tranMap = new HashMap<>();

}