## Dubbo 历史
* 销冠科技 dubbo-lite 3.0.0 ~ current
* 当当网   dubbox 2.8.0 ~ 2.8.4
* 阿里巴巴 dubbo 2.0.7 ~ 2.4.11

## 当前版本修改内容
1. 删除模块 hessian-lite,dubbo-container,dubbo-remoting-mina,dubbo-demo,dubbo-filter,dubbo-simple,dubbo-test,dubbo-tool,dubbo-admin
2. 新增模块 dubbo-boot 采用 Spring Boot 替代 dubbo-container 模块
3. 依赖版本更新 spring,grizzly,netty,hessian,slf4j-api
4. 删除 dubbo-common 模块中的 logger 包，采用 slf4j-api 替代
5. 删除代码中用 @Deprecated  标记的代码
7. 删除 jetty 相关实现
8. 删除 zkClient 相关实现
9. 解决 maven 编译过程中控制台输出的所有 warning 信息
10. 解决 maven 模块之间不合理的依赖关系