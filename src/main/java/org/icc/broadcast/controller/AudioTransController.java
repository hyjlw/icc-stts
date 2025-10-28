package org.icc.broadcast.controller;


import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.icc.broadcast.common.HttpResult;
import org.icc.broadcast.dto.AudioTransDto;
import org.icc.broadcast.service.impl.AudioScheduleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
public class AudioTransController {

    private final AudioScheduleService audioScheduleService;

    @PostMapping("/start-recognize")
    public HttpResult startRecognize(HttpServletRequest request, @RequestBody @Validated AudioTransDto audioTransDto) {

        if(StringUtils.isBlank(audioTransDto.getBroadcastId())) {
            audioTransDto.setBroadcastId(UUID.randomUUID().toString());
        }

        if(StringUtils.isBlank(audioTransDto.getProvider())) {
            audioTransDto.setProvider("AZURE");
        }

        audioScheduleService.startSession(audioTransDto);

        return new HttpResult();
    }
}
