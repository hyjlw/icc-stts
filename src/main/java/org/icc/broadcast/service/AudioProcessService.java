package org.icc.broadcast.service;

import org.icc.broadcast.ws.SocketMsg;

public interface AudioProcessService {

    void handleSocketMsg(SocketMsg socketMsg);

}
