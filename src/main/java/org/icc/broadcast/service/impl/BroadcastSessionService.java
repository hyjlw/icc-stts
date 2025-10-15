/**
 *
 */
package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.dto.BroadcastEvent;
import org.icc.broadcast.entity.BroadcastMachine;
import org.icc.broadcast.entity.BroadcastMachineConfig;
import org.icc.broadcast.entity.BroadcastSession;
import org.icc.broadcast.exception.BizException;
import org.icc.broadcast.repo.BroadcastMachineConfigRepository;
import org.icc.broadcast.repo.BroadcastMachineRepository;
import org.icc.broadcast.repo.BroadcastSessionRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

/**
 * @author LL
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BroadcastSessionService {

    private final BroadcastMachineRepository broadcastMachineRepository;
    private final BroadcastMachineConfigRepository broadcastMachineConfigRepository;
    private final BroadcastSessionRepository broadcastSessionRepository;

    private final AudioScheduleService audioScheduleService;

    public void switchBroadcastSession(BroadcastEvent broadcastEvent) {
        log.info("switch broadcast session: {}", broadcastEvent);
        BroadcastMachine machine = broadcastMachineRepository.findById(new ObjectId(broadcastEvent.getMachineId()));
        if(machine == null) {
            throw new BizException("No machine found");
        }

        if(machine.getStarted()) {
            BroadcastMachineConfig config = broadcastMachineConfigRepository.findOneBy(
                    Criteria.where("machineId").is(machine.getId())
                            .and("deleted").is(false));
            if(config == null) {
                log.warn("No machine config find for machine: {}", machine.getName());

                return;
            }

            BroadcastSession broadcastSession = broadcastSessionRepository.findById(config.getSessionId());
            if(broadcastSession == null) {
                log.warn("No broadcast session for id: {}", config.getSessionId().toHexString());
                return;
            }

            audioScheduleService.startSession(AudioTransDto.builder()
                    .sessionId(broadcastSession.getId().toHexString())
                    .srcLang(broadcastSession.getSrcLang())
                    .destLang(broadcastSession.getDestLang())
                    .destModel(broadcastSession.getDestModel())
                    .build());
        } else {
            audioScheduleService.stopSession();
        }
    }
}
