package org.icc.broadcast.runner;

import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.entity.BroadcastMachine;
import org.icc.broadcast.entity.BroadcastMachineConfig;
import org.icc.broadcast.entity.BroadcastSession;
import org.icc.broadcast.repo.BroadcastMachineConfigRepository;
import org.icc.broadcast.repo.BroadcastSessionRepository;
import org.icc.broadcast.service.impl.AudioScheduleService;
import org.icc.broadcast.service.impl.BroadcastMachineService;
import org.icc.broadcast.service.impl.MachineCommonService;
import org.icc.broadcast.utils.SpringContextHolder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Slf4j
public class AudioInitRunner implements ApplicationRunner {
 
    @Override
    public void run(ApplicationArguments args) {
        try {
            MachineCommonService machineCommonService = SpringContextHolder.getBean(MachineCommonService.class);
            String machineKey = machineCommonService.getMachineKey();

            log.info("cur machine key: {}", machineKey);

            BroadcastMachineService broadcastMachineService = SpringContextHolder.getBean(BroadcastMachineService.class);
            BroadcastMachine curMachine = broadcastMachineService.findByKey(machineKey);
            log.info("key: {}, machine: {}", machineKey, curMachine);

            if (curMachine == null || !curMachine.getStarted()) {
                return;
            }

            BroadcastMachineConfigRepository broadcastMachineConfigRepository = SpringContextHolder.getBean(BroadcastMachineConfigRepository.class);
            BroadcastMachineConfig broadcastMachineConfig = broadcastMachineConfigRepository.findOneBy(Criteria.where("machineId").is(curMachine.getId()));
            if (broadcastMachineConfig == null) {
                return;
            }

            BroadcastSessionRepository broadcastSessionRepository = SpringContextHolder.getBean(BroadcastSessionRepository.class);
            BroadcastSession broadcastSession = broadcastSessionRepository.findOneBy(Criteria.where("_id").is(broadcastMachineConfig.getSessionId()));
            if (broadcastSession == null) {
                return;
            }

            AudioScheduleService audioScheduleService = SpringContextHolder.getBean(AudioScheduleService.class);
            audioScheduleService.startSession(AudioTransDto.builder()
                    .srcLang(broadcastSession.getSrcLang())
                    .destLang(broadcastSession.getDestLang())
                    .destModel(broadcastSession.getDestModel())
                    .sessionId(broadcastSession.getId().toHexString())
                    .broadcastId(UUID.randomUUID().toString())
                    .provider("AZURE")
                    .build());
        } catch (Exception e) {
            log.error("init audio error", e);
        }
    }

}