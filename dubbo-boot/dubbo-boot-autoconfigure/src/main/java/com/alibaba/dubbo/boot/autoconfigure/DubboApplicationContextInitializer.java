package com.alibaba.dubbo.boot.autoconfigure;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Dubbo Application Initializer
 *
 * @author Gavin Hu
 * @version 3.0.0
 */
public class DubboApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        //
        if(configurableApplicationContext.getEnvironment().getProperty("dubbo.client.bootstrap", Boolean.class, true)) {
            configurableApplicationContext.addApplicationListener(new DubboClientBootstrap(configurableApplicationContext));
        }
        //
        if(configurableApplicationContext.getEnvironment().getProperty("dubbo.server.bootstrap", Boolean.class, true)) {
            configurableApplicationContext.addApplicationListener(new DubboServerBootstrap(configurableApplicationContext));
        }
    }

}
