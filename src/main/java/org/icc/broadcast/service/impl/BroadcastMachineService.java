package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.entity.BroadcastMachine;
import org.icc.broadcast.repo.BroadcastMachineRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class BroadcastMachineService {

    private final BroadcastMachineRepository broadcastMachineRepository;

    private final MachineCommonService machineCommonService;

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void registerSttsMachine() {
        log.info("register stts machine...");

        String port = machineCommonService.getHostPort();
        String hostname = machineCommonService.getHostname();
        String ip = machineCommonService.getHostAddress();
        String key = machineCommonService.getMachineKey();

        BroadcastMachine machine = broadcastMachineRepository.findOneBy(Criteria.where("key").is(key));
        if(machine != null) {
            broadcastMachineRepository.updateTime(machine.getId(), new Date());
        } else {
            BroadcastMachine broadcastMachine = BroadcastMachine.builder()
                    .key(key)
                    .name(hostname)
                    .ip(ip)
                    .port(port)
                    .started(false)
                    .active(true)
                    .deleted(false)
                    .createAt(new Date())
                    .updateTime(new Date())
                    .build();

            broadcastMachineRepository.add(broadcastMachine);
        }
    }

}
