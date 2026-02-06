package org.icc.broadcast.kafka;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.icc.broadcast.dto.BroadcastEvent;
import org.icc.broadcast.service.impl.BroadcastSessionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BroadcastSessionMessageListener {
    private final BroadcastSessionService broadcastSessionService;

    @KafkaListener(topics = { "BROADCAST_EVENT" }, concurrency = "2")
    public void receiveBroadcastEventMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("【*** Broadcast Message: ***】key = {}, value = {}", record.key(), record.value());

        try {
            BroadcastEvent broadcastEvent = JSONObject.parseObject(record.value(), BroadcastEvent.class);
            broadcastSessionService.switchBroadcastSession(broadcastEvent);
        } catch (Exception e) {
            log.error("error in Broadcast:", e);
        } finally {
            ack.acknowledge();
        }
    }

}
