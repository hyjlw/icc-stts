package org.icc.broadcast.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

public final class ThreadMdcUtil {
    private static final String TRACE_ID = "TRACE_ID";

    // 获取唯一性标识
    public static String generateTraceId() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    public static void setTraceIdIfAbsent() {
        if (MDC.get(TRACE_ID) == null) {
            MDC.put(TRACE_ID, generateTraceId());
        }
    }

    /**
     * 用于父线程向线程池中提交任务时，将自身MDC中的数据复制给子线程
     * @author freedom
     * @date 2022/12/28 17:52
     * @param callable callable
     * @param context  context
     * @return {@link Callable<T>}
     */
    public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }


    /**
     * 用于父线程向线程池中提交任务时，将自身MDC中的数据复制给子线程
     * @author freedom
     * @date 2022/12/28 17:52
     * @param runnable runnable
     * @param context  context
     * @return {@link Runnable}
     */
    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
