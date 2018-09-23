package com.mtl.demo.serviceC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {RedisRepositoriesAutoConfiguration.class, MongoAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableFeignClients
@EnableDiscoveryClient
@EnableEurekaClient
@ComponentScan({"com.mtl.hulk", "com.mtl.demo.serviceC"})
@PropertySource({"file:/apps/config/hulk/hulk_global_test.properties", "file:/apps/config/hulk/hulk.properties"})
public class ServiceCApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCApplication.class, args);
    }
}