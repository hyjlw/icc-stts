package org.icc.broadcast.kafka;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bson.types.ObjectId;
import org.icc.broadcast.dto.BroadcastEvent;
import org.icc.broadcast.entity.BroadcastSession;
import org.icc.broadcast.service.impl.BroadcastSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BroadcastSessionMessageListener {
    @Autowired
    private BroadcastSessionService broadcastSessionService;

    @KafkaListener(topics = { "BROADCAST_EVENT" }, concurrency = "2")
    public void receiveBroadcastEventMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("【*** Broadcast Message: ***】key = {}, value = {}", record.key(), record.value());

        try {
            BroadcastEvent broadcastEvent = JSONObject.parseObject(record.value(), BroadcastEvent.class);

            broadcastSessionService.switchBroadcastSession(BroadcastSession.builder()
                    .id(new ObjectId(broadcastEvent.getBroadcastSessionId()))
                            .started("start".equals(broadcastEvent.getEvent()))
                    .build());
        } catch (Exception e) {
            log.error("error in Broadcast:", e);
        } finally {
            ack.acknowledge();
        }
    }

}
