package com.mtl.hulk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@PropertySource("classpath:hulk.properties")
public class HulkApplication {

    public static void main(String[] args) {
        SpringApplication.run(HulkApplication.class, args);
    }

}
