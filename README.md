## Dubbo 历史
* 当当网   dubbox 2.8.0 ~ 2.8.4
* 阿里巴巴 dubbo 2.0.7 ~ 2.4.11

## 当前版本修改内容
1. 删除模块 hessian-lite,dubbo-container,dubbo-remoting-mina,dubbo-filter,dubbo-simple,dubbo-test,dubbo-tool,dubbo-admin
2. 新增模块 dubbo-boot 采用 Spring Boot 替代 dubbo-container 模块
3. 依赖版本更新 spring,grizzly,netty,hessian,slf4j-api
4. 删除 dubbo-common 模块中的 logger 包，采用 slf4j-api 替代
5. 删除代码中用 @Deprecated  标记的代码
7. 删除 jetty 相关实现
8. 删除 zkClient 相关实现
9. 解决 maven 编译过程中控制台输出的所有 warning 信息
10. 解决 maven 模块之间不合理的依赖关系
11. dubbo-demo 模块使用 spring-boot 应用示例

## 如何使用？

### 服务端工作
1. 服务接口定义
```
public interface HelloService {

    String hello(String message);
    
}
```

2. 服务接口实现
```
@Service
public class DefaultHelloService implements HelloService {

    @Override
    public String hello(String message) {
        return "hello, " + message;
    }

}
```

3. 服务接口暴露自动化配置
```
<dependency>
    <groupId>com.alibaba.dubbolite</groupId>
    <artifactId>dubbo-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

```
# dubbo 服务端启用开关
dubbo.server.bootstrap: true
# dubbo 服务端需要暴露的服务接口所在的包
dubbo.servicePackages: com.alibaba.dubbo.demo.service
#
dubbo.application.name: dubbo-demo-provider
# dubbo 服务注册中心配置
dubbo.registry.address: zookeeper://127.0.0.1:2181
#dubbo.registry.address=multicast://224.5.6.7:1234
#dubbo.registry.address=redis://127.0.0.1:6379
#dubbo.registry.address=dubbo://127.0.0.1:9090
# dubbo 协议配置
dubbo.protocol.name: dubbo
dubbo.protocol.port: 20880
```

### 客户端工作

1. 服务接口依赖引入
```
<dependency>
    <groupId>com.alibaba.dubbolite</groupId>
    <artifactId>dubbo-demo-api</artifactId>
    <version>${project.version}</version>
</dependency>
```

2. 服务接口注入
```
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
```

3. 服务接口引入自动化配置
```
<dependency>
    <groupId>com.alibaba.dubbolite</groupId>
    <artifactId>dubbo-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

```
# dubbo 客户端启用开关
dubbo.client.bootstrap: true
# dubbo 客户端服务接口所在的包
dubbo.referencePackages: com.alibaba.dubbo.demo.service
# dubbo 客户端应用名，不要与服务端重名
dubbo.application.name: dubbo-demo-consumer
# dubbo 服务注册中心配置
dubbo.registry.address: zookeeper://127.0.0.1:2181
#dubbo.registry.address=multicast://224.5.6.7:1234
#dubbo.registry.address=redis://127.0.0.1:6379
#dubbo.registry.address=dubbo://127.0.0.1:9090
# dubbo 协议配置
dubbo.protocol.name: dubbo
dubbo.protocol.port: 20880
```