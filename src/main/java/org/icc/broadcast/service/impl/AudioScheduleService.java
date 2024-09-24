package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.ws.AudioWebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioScheduleService {

    @Value("${audio.socket.url}")
    private String socketUrl;

    private AudioWebSocketClient audioWebSocketClient;

    private final AudioProcessService audioProcessService;

    @PostConstruct
    public void initWsClient() throws URISyntaxException {
        AudioWebSocketClient client = new AudioWebSocketClient(new URI(socketUrl));
        client.connect();
        log.info("started ws client: {}", client);

        this.audioWebSocketClient = client;
    }

    public void startSession(AudioTransDto audioTransDto) {
        audioProcessService.startToHandleAudio(audioTransDto);

        if(audioWebSocketClient != null) {
            audioWebSocketClient.setFlag(true);
        }
    }
}
