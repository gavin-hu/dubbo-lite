package com.alibaba.dubbo.demo.consumer;

import com.alibaba.dubbo.demo.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private HelloService helloService;

    @Override
    public void run(String... strings) throws Exception {
        //
        String result = helloService.hello("gavin");
        //
        System.out.println(result);
    }
}
