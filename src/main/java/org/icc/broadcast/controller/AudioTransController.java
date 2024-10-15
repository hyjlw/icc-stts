package org.icc.broadcast.controller;


import lombok.RequiredArgsConstructor;
import org.icc.broadcast.common.HttpResult;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.service.impl.AudioScheduleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/icc-stts/audio")
@RequiredArgsConstructor
public class AudioTransController {

    private final AudioScheduleService audioScheduleService;

    @PostMapping("/start-recognize")
    public HttpResult startRecognize(HttpServletRequest request, @RequestBody @Validated AudioTransDto audioTransDto) {
        audioScheduleService.startSession(audioTransDto);

        return new HttpResult();
    }
}
