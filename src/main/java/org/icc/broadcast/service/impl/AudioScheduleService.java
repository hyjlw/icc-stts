package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.common.HttpResultCode;
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

    @Setter
    private volatile boolean started = false;

    @PostConstruct
    public void initWsClient() throws URISyntaxException {
        AudioWebSocketClient client = new AudioWebSocketClient(new URI(socketUrl));
        client.connect();
        log.info("started ws client: {}", client);

        this.audioWebSocketClient = client;
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

                throw  new BizException(HttpResultCode.WS_INIT_ERROR);
            }
        }
        audioWebSocketClient.setFlag(true);

        this.started = true;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void checkSession() {
        if(this.started) {
            return;
        }

        BroadcastSession session = broadcastSessionRepository.findOneBy(new Criteria());
        if(session == null) {
            return;
        }

        if(!session.getStarted()) {
            return;
        }

        Date now = new Date();
        if(now.compareTo(session.getEndTime()) > 0 || now.compareTo(session.getStartTime()) < 0) {
            return;
        }

        this.startSession(AudioTransDto.builder()
                .sessionId(session.getId().toHexString())
                .srcLang(session.getSrcLang())
                .destLang(session.getDestLang())
                .destModel(session.getDestModel())
                .build());
    }
}
