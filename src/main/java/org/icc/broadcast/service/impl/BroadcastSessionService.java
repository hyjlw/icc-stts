/**
 *
 */
package org.icc.broadcast.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.dto.CommonResp;
import org.icc.broadcast.dto.QueryCriteria;
import org.icc.broadcast.entity.BroadcastSession;
import org.icc.broadcast.exception.BizException;
import org.icc.broadcast.repo.BroadcastSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    public CommonResp saveBroadcastSession(BroadcastSession broadcastSession) {
        log.info("save broadcast session: {}", broadcastSession);

        broadcastSession.setCreateAt(new Date());
        broadcastSessionRepository.add(broadcastSession);

        return CommonResp.builder().msg("ok").success(true).data(broadcastSession.getId().toHexString()).build();
    }

    public CommonResp updateBroadcastSession(BroadcastSession broadcastSession) {
        log.info("update broadcast session: {}", broadcastSession);

        broadcastSessionRepository.add(broadcastSession);

        return CommonResp.builder().msg("ok").success(true).data(broadcastSession.getId().toHexString()).build();
    }

    public Page<BroadcastSession> loadBroadcastSessions(QueryCriteria queryCriteria, Pageable pageable) {
        log.info("load broadcast sessions: {}", queryCriteria);

        int start = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        Criteria criteria = new Criteria();
        List<BroadcastSession> broadcastSessions = broadcastSessionRepository.findBy(criteria, start, limit);
        long total = broadcastSessionRepository.count(criteria);

        return new PageImpl<>(broadcastSessions, pageable, total);
    }

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

            broadcastSessionRepository.updateStarted(session.getId(), true);
            broadcastSessionRepository.updateTime(session.getId(), new Date());
        } else {
            audioScheduleService.stopSession();
            broadcastSessionRepository.updateStarted(session.getId(), false);
        }

        return CommonResp.builder().msg("ok").success(true).data(broadcastSession.getId().toHexString()).build();
    }
}
