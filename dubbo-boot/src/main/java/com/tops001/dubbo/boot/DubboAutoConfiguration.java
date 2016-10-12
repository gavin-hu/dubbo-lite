package com.tops001.dubbo.boot;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo Auto Configuration
 *
 * @author Gavin Hu
 * @version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DubboConfiguration.class)
public class DubboAutoConfiguration {

    @Autowired
    private DubboConfiguration dubboConfiguration;

    @Bean
    public ApplicationConfig applicationConfig() {
        return dubboConfiguration.getApplication();
    }

    @Bean
    public RegistryConfig registryConfig() {
        return dubboConfiguration.getRegistry();
    }

    @Bean
    public ProtocolConfig protocolConfig() {
        return dubboConfiguration.getProtocol();
    }

}
