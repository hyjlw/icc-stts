package org.icc.broadcast.controller;

import lombok.RequiredArgsConstructor;
import org.icc.broadcast.dto.CommonResp;
import org.icc.broadcast.dto.QueryCriteria;
import org.icc.broadcast.entity.BroadcastSession;
import org.icc.broadcast.service.impl.BroadcastSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BroadcastSessionController {

    private final BroadcastSessionService broadcastSessionService;

    @PostMapping("/sim-int")
    public ResponseEntity<Object> saveBroadcastSession(@RequestBody BroadcastSession broadcastSession) {
        CommonResp commonResp = broadcastSessionService.saveBroadcastSession(broadcastSession);

        return new ResponseEntity<>(commonResp, HttpStatus.CREATED);
    }

    @PutMapping("/sim-int")
    public ResponseEntity<Object> updateBroadcastSession(@RequestBody BroadcastSession broadcastSession) {
        CommonResp commonResp = broadcastSessionService.updateBroadcastSession(broadcastSession);

        return new ResponseEntity<>(commonResp, HttpStatus.OK);
    }

    @GetMapping("/sim-int")
    public ResponseEntity<Object> loadBroadcastSession(QueryCriteria queryCriteria, Pageable pageable) {
        Page<BroadcastSession> page = broadcastSessionService.loadBroadcastSessions(queryCriteria, pageable);

        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PutMapping("/sim-int/switch")
    public ResponseEntity<Object> startBroadcastSession(@RequestBody BroadcastSession broadcastSession) {
        CommonResp commonResp = broadcastSessionService.switchBroadcastSession(broadcastSession);

        return new ResponseEntity<>(commonResp, HttpStatus.CREATED);
    }

}
