package org.icc.broadcast.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.velocity.shaded.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class FileService {
    public String downloadFile(String baseDir, String url) {
        log.info("start to download url: {}", url);

        String fileName = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));

            HttpEntity<Object> entity = new HttpEntity<>(headers);

            final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            final HttpClient httpClient = HttpClientBuilder.create()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            factory.setHttpClient(httpClient);
            restTemplate.setRequestFactory(factory);

            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            if(responseEntity.getBody() == null) {
                return null;
            }

            fileName = baseDir + "/" + FilenameUtils.getName(url);
            int index = fileName.indexOf(".");
            index = fileName.indexOf('_', index);
            if(index > -1) {
                fileName = fileName.substring(0, index);
            }

            Files.write(Paths.get(fileName), Objects.requireNonNull(responseEntity.getBody()));
        } catch (Exception e) {
            log.error("download file error: {}", e.getMessage());
        }

        return fileName;
    }

}
