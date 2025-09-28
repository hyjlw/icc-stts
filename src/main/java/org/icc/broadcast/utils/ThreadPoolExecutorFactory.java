package org.icc.broadcast.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolExecutorFactory{

    public static Executor get() {
        LocalThreadPoolTaskExecutor executor = new LocalThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(0);
        executor.initialize();
        return executor;
    }

    public static Executor get(Integer queueSize) {
        LocalThreadPoolTaskExecutor executor = new LocalThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(queueSize);
        executor.setKeepAliveSeconds(0);
        executor.initialize();
        return executor;
    }

    public static Executor getSingle(Integer queueSize) {
        LocalThreadPoolTaskExecutor executor = new LocalThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(queueSize);
        executor.setKeepAliveSeconds(0);
        executor.initialize();
        return executor;
    }


    public static Executor getCallerRunsPolicy(Integer queueSize){

        LocalThreadPoolTaskExecutor executor = new LocalThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(queueSize);
        executor.setKeepAliveSeconds(0);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }


}
