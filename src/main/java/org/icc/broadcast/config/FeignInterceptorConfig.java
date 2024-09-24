package org.icc.broadcast.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FeignInterceptorConfig {
 
    @Bean
    public RequestInterceptor customRequestInterceptor(){
        return new FeignInterceptor();
    }
}