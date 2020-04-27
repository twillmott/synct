package io.twillmott.synct.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    /**
     * Task scheduler used to poll for trakt authorization.
     */
    @Bean
    public ThreadPoolTaskScheduler traktAuthThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setThreadNamePrefix("auth-scheduler-");
        return threadPoolTaskScheduler;
    }

}
