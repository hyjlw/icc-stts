/**
 *
 */
package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.dto.CommonResp;
import org.icc.broadcast.entity.BroadcastSession;
import org.icc.broadcast.exception.BizException;
import org.icc.broadcast.repo.BroadcastSessionRepository;
import org.springframework.stereotype.Service;

/**
 * @author LL
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BroadcastSessionService {

    private final BroadcastSessionRepository broadcastSessionRepository;
    private final AudioScheduleService audioScheduleService;


    public CommonResp switchBroadcastSession(BroadcastSession broadcastSession) {
        log.info("switch broadcast session: {}", broadcastSession);
        BroadcastSession session = broadcastSessionRepository.findById(broadcastSession.getId());
        if(session == null) {
            throw new BizException("No session found");
        }

        if(broadcastSession.started) {
            audioScheduleService.startSession(AudioTransDto.builder()
                    .sessionId(session.getId().toHexString())
                    .srcLang(session.getSrcLang())
                    .destLang(session.getDestLang())
                    .destModel(session.getDestModel())
                    .build());
        } else {
            audioScheduleService.stopSession();
        }

        return CommonResp.builder().msg("ok").success(true).data(broadcastSession.getId().toHexString()).build();
    }
}
