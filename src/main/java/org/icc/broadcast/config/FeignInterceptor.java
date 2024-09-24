package org.icc.broadcast.config;

import org.icc.broadcast.constant.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignInterceptor implements RequestInterceptor {
 
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("AUTH_KEY", Constants.AUTH_KEY);
    }
 
}