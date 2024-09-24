package org.icc.broadcast.controller;


import lombok.RequiredArgsConstructor;
import org.icc.broadcast.common.HttpResult;
import org.icc.broadcast.service.impl.StreamDownloadService;
import org.icc.broadcast.service.impl.VideoTranslationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionTransController {

    private final VideoTranslationService videoTranslationService;

    private final StreamDownloadService streamDownloadService;

    @PostMapping("/update-index")
    public HttpResult updateMediaIndex(HttpServletRequest request)
    {
        videoTranslationService.updateMediaIndex();

        return new HttpResult();
    }

    @PostMapping("/startup")
    public HttpResult startup(HttpServletRequest request)
    {
        videoTranslationService.resetIndex();
        streamDownloadService.startupVideoTrans();

        return new HttpResult();
    }

    @PostMapping("/shutdown")
    public HttpResult shutdown(HttpServletRequest request)
    {
        streamDownloadService.shutdownVideoTrans();

        return new HttpResult();
    }
}
