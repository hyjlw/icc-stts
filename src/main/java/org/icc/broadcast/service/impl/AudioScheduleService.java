package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.common.HttpResultCode;
import org.icc.broadcast.config.SttsConfig;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.entity.BroadcastSession;
import org.icc.broadcast.exception.BizException;
import org.icc.broadcast.repo.BroadcastSessionRepository;
import org.icc.broadcast.ws.AudioWebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioScheduleService {

    @Value("${audio.socket.url}")
    private String socketUrl;

    private AudioWebSocketClient audioWebSocketClient;
    private final RcgAudioProcessService rcgAudioProcessService;
    private final RawAudioProcessService rawAudioProcessService;

    private final SttsConfig sttsConfig;

    @Setter
    private volatile boolean started = false;

    @PostConstruct
    public void initWsClient() {
        try {
            AudioWebSocketClient client = new AudioWebSocketClient(new URI(socketUrl));
            client.setAudioProcessService(rawAudioProcessService);
            client.connect();

            log.info("started ws client: {}", client);

            this.audioWebSocketClient = client;
        } catch (URISyntaxException e) {
            log.error("init ws client error", e);
        }
    }

    public void startSession(AudioTransDto audioTransDto) {
        if(audioWebSocketClient == null) {
            log.warn("WS is not inited");
            throw new BizException(HttpResultCode.WS_INIT_ERROR);
        }

        // check closed status
        if(audioWebSocketClient.getConnection().isClosed()) {
            try {
                audioWebSocketClient = new AudioWebSocketClient(new URI(socketUrl));
                audioWebSocketClient.connect();
            } catch (URISyntaxException e) {
                log.error("parse uri error, ", e);
                throw new BizException(HttpResultCode.WS_INIT_ERROR);
            }
        }

        rcgAudioProcessService.startToHandleAudio(audioTransDto);
        audioWebSocketClient.setAudioProcessService(rcgAudioProcessService);

        sttsConfig.setSttsStarted(true);

        this.started = true;
    }

    public void stopSession() {
        sttsConfig.setSttsStarted(false);
        this.started = false;

        audioWebSocketClient.setAudioProcessService(rawAudioProcessService);
    }

    public void onWsClosed() {
        log.info("ws client closed, clear client info");
        sttsConfig.setSttsStarted(false);
        this.started = false;

        this.audioWebSocketClient = null;
    }

    @Scheduled(initialDelay = 10, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void checkClient() {
        if(started) {
            return;
        }

        if(audioWebSocketClient != null) {
            return;
        }

        log.info("current no client, try to init again...");
        initWsClient();
    }
}
