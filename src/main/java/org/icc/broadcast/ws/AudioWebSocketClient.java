package org.icc.broadcast.ws;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.service.AudioProcessService;
import org.icc.broadcast.service.impl.AudioScheduleService;
import org.icc.broadcast.utils.SpringContextHolder;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class AudioWebSocketClient extends WebSocketClient{

    private final static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    @Setter
    private AudioProcessService audioProcessService;
 
    public AudioWebSocketClient(URI serverUri) {
         super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        log.info("ws open");
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        log.info("------ AudioWebSocketClient onClose ------");

        executorService.schedule(() -> {
            AudioScheduleService audioScheduleService = SpringContextHolder.getBean(AudioScheduleService.class);
            if(audioScheduleService == null) {
                return;
            }

            log.info("current ws client closed, clear the client info in schedule service");
            audioScheduleService.onWsClosed();
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onError(Exception arg0) {
        log.info("------ AudioWebSocketClient onError ------");
    }

    @Override
    public void onMessage(String msg) {
        log.debug("-------- 接收到服务端数据： {}--------", msg);

        if(StringUtils.isBlank(msg)) {
            return;
        }

        SocketMsg socketMsg = JSONObject.parseObject(msg, SocketMsg.class);
        getService().handleSocketMsg(socketMsg);
    }

    private AudioProcessService getService() {
        return audioProcessService;
    }

}