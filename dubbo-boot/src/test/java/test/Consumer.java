package test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import test.service.HelloService;

@SpringBootApplication
public class Consumer implements CommandLineRunner {

    @Autowired
    private HelloService helloService;

    @Override
    public void run(String... strings) throws Exception {
        String hello = helloService.hello("world"); // 执行远程方法
        System.out.println(hello);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Consumer.class, args);
    }

}