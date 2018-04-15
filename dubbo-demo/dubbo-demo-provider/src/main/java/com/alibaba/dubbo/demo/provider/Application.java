package com.alibaba.dubbo.demo.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException {
        //
        SpringApplication.run(Application.class, args);
        //
        System.in.read(); // 按任意键退出
    }

}
