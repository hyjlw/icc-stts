package org.icc.broadcast.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioByteInfo;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.dto.PushAudioInfo;
import org.icc.broadcast.service.AudioProcessService;
import org.icc.broadcast.ws.SocketMsg;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class RawAudioProcessService implements AudioProcessService {

    private final AudioTranslationService audioTranslationService;

    @Setter
    private volatile String destLang;
    @Setter
    private volatile String destLangModel;
    @Setter
    private volatile String sessionId;

    @Override
    public void handleSocketMsg(SocketMsg socketMsg) {
        PushAudioInfo pushAudioInfo = JSONObject.parseObject(JSON.toJSONString(socketMsg.getData()), PushAudioInfo.class);

        AudioInfo audioInfo = AudioInfo.builder()
                .serialId(pushAudioInfo.getSerialId())
                .sessionId(sessionId)
                .srcLang(pushAudioInfo.getSrcLang())
                .destLang(destLang)
                .destModel(destLangModel)
                .rawText(pushAudioInfo.getText())
                .timestamp(pushAudioInfo.getTimestamp())
                .audioMetas(new ArrayList<>())
                .times(new ArrayList<>())
                .build();

        audioTranslationService.translateAudio(audioInfo);
    }
}
