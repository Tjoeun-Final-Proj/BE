package com.tjoeun.boxmon;

import com.tjoeun.boxmon.global.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(AppConfig.class)
public class BoxmonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoxmonApplication.class, args);
    }

}
