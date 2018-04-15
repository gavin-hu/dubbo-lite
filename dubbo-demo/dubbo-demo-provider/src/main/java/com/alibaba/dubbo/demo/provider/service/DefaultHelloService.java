package com.alibaba.dubbo.demo.provider.service;

import com.alibaba.dubbo.demo.service.HelloService;
import org.springframework.stereotype.Service;

@Service
public class DefaultHelloService implements HelloService {

    @Override
    public String hello(String message) {
        return "hello, " + message;
    }

}
