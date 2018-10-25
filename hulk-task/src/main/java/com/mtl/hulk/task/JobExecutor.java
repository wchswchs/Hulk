package com.mtl.hulk.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SuppressWarnings("all")
@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"com.mtl.demo.service.service", "com.mtl.demo.serviceB.service", "com.mtl.demo.serviceC.service", "com.mtl.hulk"}/*, basePackageClasses = {ApplicationContextUtils.class}*/)
@EnableFeignClients("com.mtl.demo.service.feign")
@PropertySource({"file:/opt/hulk/hulk_global.properties", "file:/opt/hulk/hulk.properties"})
public class JobExecutor {
    public static void main(final String[] args) {
        SpringApplication.run(JobExecutor.class, args);
    }

}
