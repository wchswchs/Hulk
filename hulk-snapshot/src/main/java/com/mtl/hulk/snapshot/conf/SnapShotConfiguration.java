package com.mtl.hulk.snapshot.conf;

import com.mtl.hulk.snapshot.SnapShot;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnapShotProperties.class)
public class SnapShotConfiguration {

    private final SnapShotProperties properties;

    public SnapShotConfiguration(SnapShotProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SnapShot snapShot() {
        return new SnapShot(properties);
    }

}
