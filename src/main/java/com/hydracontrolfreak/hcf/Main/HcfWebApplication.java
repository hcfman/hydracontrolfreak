package com.hydracontrolfreak.hcf.Main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.hydracontrolfreak"})
public class HcfWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HcfWebApplication.class, args);
    }
}
