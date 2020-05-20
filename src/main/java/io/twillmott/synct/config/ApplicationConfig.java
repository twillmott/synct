package io.twillmott.synct.config;

import com.uwetrottmann.trakt5.TraktV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@PropertySource("classpath:secrets.properties")
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

    @Autowired
    private Environment env;

    @Bean
    public TraktV2 getTraktV2() {
        return new TraktV2(
                env.getProperty("secrets.trakt.client-id"),
                env.getProperty("secrets.trakt.client-secret"),
                env.getProperty("secrets.trakt.redirect-uri"));
    }
}