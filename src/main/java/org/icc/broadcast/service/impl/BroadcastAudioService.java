package org.icc.broadcast.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.icc.broadcast.constant.ProcessType;
import org.icc.broadcast.dto.AudioInfo;
import org.icc.broadcast.entity.TtsTime;
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

            if(!CollectionUtil.isEmpty(audioInfo.getAudioMetas())) {
                broadcastAudioRepository.addAudioMetas(serialId, audioInfo.getAudioMetas());
            }

            if(!CollectionUtil.isEmpty(audioInfo.getTimes())) {
//                broadcastAudioRepository.addProcessTimes(serialId, audioInfo.getTimes());

                TtsTime ttsTime = TtsTime.builder()
                        .text(audioInfo.getRawText())
                        .segmentId(audioInfo.getSegmentId())
                        .totalTime(0)
                        .build();

                audioInfo.getTimes().forEach(t -> {
                    if(ProcessType.TRANSLATION.getCode().equals(t.getType())) {
                        ttsTime.setTranslation(t);
                    } else if(ProcessType.SYNTHESISE.getCode().equals(t.getType())) {
                        ttsTime.setSynthesize(t);
                    }

                    ttsTime.setTotalTime(ttsTime.getTotalTime() + t.getDuration());
                });

                broadcastAudioRepository.addTtsTimes(serialId, Lists.newArrayList(ttsTime));
            }

        });
    }

}
