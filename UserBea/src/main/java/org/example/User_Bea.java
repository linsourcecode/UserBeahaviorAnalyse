package org.example;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@MapperScan("org.example.mapper")
//@EnableDiscoveryClient
@EnableScheduling
public class User_Bea {
    public static void main(String[] args) {
        SpringApplication.run(User_Bea.class, args);}
}
