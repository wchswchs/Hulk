package com.mtl.hulk.snapshot.conf;

import com.mtl.hulk.snapshot.Snapshot1;
import com.mtl.hulk.snapshot.SnapshotFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SnapshotProperties.class)
public class SnapshotConfiguration1 {

    private final SnapshotFactory factory;

    public SnapshotConfiguration1(SnapshotProperties properties) {
        this.factory = new SnapshotFactory(properties);
    }

    @Bean
    public Snapshot1 snapshot() {
        SnapshotProperties properties = factory.getProperties();
        return factory.createSnapshot(properties.getBufferSize(), properties.getPerFileSize());
    }

}
