package test.service;

import org.springframework.stereotype.Service;

/**
 * @author Gavin Hu
 * @version 1.0.0
 */
@Service
public class DefaultHelloService implements HelloService {

    @Override
    public String hello(String message) {
        return String.format("hello, %s", message);
    }

}
