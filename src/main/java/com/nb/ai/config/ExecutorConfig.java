package com.nb.ai.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Bean(name = "aiStreamingExecutor")
    public ExecutorService aiStreamingExecutor() {

        return new ThreadPoolExecutor(
                10,                      // corePoolSize
                20,                      // maximumPoolSize
                60,                      // keepAliveTime
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger counter =
                            new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread =
                                new Thread(runnable);

                        thread.setName(
                                "ai-streaming-" + counter.getAndIncrement()
                        );

                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
