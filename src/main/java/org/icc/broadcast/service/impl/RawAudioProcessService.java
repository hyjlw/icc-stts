package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioByteInfo;
import org.icc.broadcast.service.AudioProcessService;
import org.icc.broadcast.ws.SocketMsg;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RawAudioProcessService implements AudioProcessService {

    private final AudioPlayService audioPlayService;

    @Override
    public void handleSocketMsg(SocketMsg socketMsg) {
        AudioByteInfo audioByteInfo = AudioByteInfo.builder()
                .timestamp(socketMsg.getTimestamp())
                .seq(0)
                .bytes(socketMsg.getData())
                .build();

        audioPlayService.playAudioByte(audioByteInfo);
    }
}
