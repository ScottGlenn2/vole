package com.github.vole.casclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class VoleCasclientApplication {

    public static void main(String[] args) {
        log.debug("VoleCasclientApplication startup main");
        SpringApplication application = new SpringApplication(VoleCasclientApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }
}
