package org.icc.broadcast.ws;

import java.net.URI;

import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.service.impl.AudioProcessService;
import org.icc.broadcast.utils.SpringContextHolder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class AudioWebSocketClient extends WebSocketClient{

    private AudioProcessService audioProcessService;

    @Setter
    private volatile boolean flag;
 
    public AudioWebSocketClient(URI serverUri) {
         super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        log.info("ws open");
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        log.info("------ MyWebSocket onClose ------");
    }

    @Override
    public void onError(Exception arg0) {
        log.info("------ MyWebSocket onError ------");
    }

    @Override
    public void onMessage(String msg) {
        log.debug("-------- 接收到服务端数据： {}--------", msg);

        if(!flag) {
            return;
        }

        if(StringUtils.isBlank(msg)) {
            return;
        }

        SocketMsg socketMsg = JSONObject.parseObject(msg, SocketMsg.class);
        getService().handleAudioData(socketMsg);
    }

    private AudioProcessService getService() {
        if(audioProcessService == null) {
            audioProcessService = SpringContextHolder.getBean(AudioProcessService.class);
        }

        return audioProcessService;
    }

}