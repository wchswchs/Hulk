package com.mtl.demo.serviceB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {RedisRepositoriesAutoConfiguration.class, MongoAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableFeignClients
@EnableEurekaClient
@ComponentScan({"com.mtl.hulk", "com.mtl.demo.serviceB"})
@PropertySource({"file:/apps/config/hulk/hulk_global_test.properties", "file:/apps/config/hulk/hulk.properties"})
public class ServiceBApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceBApplication.class, args);
    }
}