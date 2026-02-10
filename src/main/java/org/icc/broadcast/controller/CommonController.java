package org.icc.broadcast.controller;


import lombok.RequiredArgsConstructor;
import org.icc.broadcast.common.HttpResult;
import org.icc.broadcast.service.impl.MachineCommonService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final MachineCommonService machineCommonService;

    @GetMapping("/get-machine-key")
    public HttpResult startRecognize(HttpServletRequest request) {
        return new HttpResult(machineCommonService.getMachineKey());
    }
}
