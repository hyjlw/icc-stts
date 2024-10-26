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
    private final AudioProcessService audioProcessService;
    private final BroadcastSessionRepository broadcastSessionRepository;

    private final SttsConfig sttsConfig;

    @Setter
    private volatile boolean started = false;

    @PostConstruct
    public void initWsClient() throws URISyntaxException {
        AudioWebSocketClient client = new AudioWebSocketClient(new URI(socketUrl));
        client.connect();
        log.info("started ws client: {}", client);

        this.audioWebSocketClient = client;
        audioProcessService.startToHandleAudio(AudioTransDto.builder().sessionId(System.currentTimeMillis() + "").build());
    }

    public void startSession(AudioTransDto audioTransDto) {
        audioProcessService.startToHandleAudio(audioTransDto);

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

        sttsConfig.setSttsStarted(true);

        this.started = true;
    }

    public void stopSession() {
        sttsConfig.setSttsStarted(false);
        this.started = false;
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void checkSession() {
        if(!started) {
            return;
        }

        BroadcastSession session = broadcastSessionRepository.findOneBy(Criteria.where("started").is(true));
        if(session == null) {
            return;
        }

        broadcastSessionRepository.updateTime(session.getId(), new Date());
    }
}
