package org.icc.broadcast.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.repo.BroadcastAudioRepository;
import org.icc.broadcast.utils.ThreadPoolExecutorFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class BroadcastAudioService {
    private static final Executor PERSIST_POOL = ThreadPoolExecutorFactory.get(10000);

    private final BroadcastAudioRepository broadcastAudioRepository;

    public void saveAudioInfo(AudioInfo audioInfo) {
        log.info("save audio info: {}", audioInfo);

        PERSIST_POOL.execute(() -> {
            long serialId = audioInfo.getSerialId();

            if(CollectionUtil.isEmpty(audioInfo.getAudioMetas())) {
                broadcastAudioRepository.addAudioMetas(serialId, audioInfo.getAudioMetas());
            }

            if(CollectionUtil.isEmpty(audioInfo.getTimes())) {
                broadcastAudioRepository.addProcessTimes(serialId, audioInfo.getTimes());
            }

        });
    }

}
