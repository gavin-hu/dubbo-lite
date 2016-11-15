package test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Provider {
 
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Provider.class, args);
        //
        System.in.read(); // 按任意键退出
    }
 
}