package com.tops001.dubbo.boot;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dubbo Client Bootstrap
 *
 * @author Gavin Hu
 * @version 1.0.0
 */
public class DubboClientBootstrap implements ApplicationListener<ApplicationContextEvent>, BeanFactoryPostProcessor, MethodInterceptor {

    private Object lock = new Object();

    private ConfigurableApplicationContext configurableApplicationContext;

    private Map<Class, ReferenceConfig> referenceConfigs = new ConcurrentHashMap<>();

    private MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    public DubboClientBootstrap(ConfigurableApplicationContext configurableApplicationContext) {
        this.configurableApplicationContext = configurableApplicationContext;
        //
        configurableApplicationContext.addBeanFactoryPostProcessor(this);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            registerReferenceProxies(beanFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent applicationContextEvent) {
        if(applicationContextEvent instanceof ContextClosedEvent) {
            try {
                unimportReferences(applicationContextEvent.getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerReferenceProxies(ConfigurableListableBeanFactory beanFactory) throws Exception {
        //
        String[] referencePackages = configurableApplicationContext.getEnvironment().getProperty("dubbo.referencePackages", String[].class);
        if(referencePackages!=null) {
            for (String referencePackage : referencePackages) {
                String scanPattern = "classpath:" + ClassUtils.convertClassNameToResourcePath(referencePackage) + "/**/*";
                Resource[] resources = configurableApplicationContext.getResources(scanPattern);
                for (Resource resource : resources) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    if (metadataReader.getClassMetadata().isInterface()) {
                        Class referenceInterface = configurableApplicationContext.getClassLoader().loadClass(metadataReader.getClassMetadata().getClassName());
                        //
                        String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(configurableApplicationContext, referenceInterface);
                        if(beanNames.length>0) {
                            continue;
                        }
                        //
                        Object referenceProxy = ProxyFactory.getProxy(referenceInterface, this);
                        //
                        beanFactory.registerSingleton(referenceInterface.getName(), referenceProxy);
                    }
                }
            }
        }

    }

    public void unimportReferences(ApplicationContext applicationContext) throws Exception {
        referenceConfigs.values().forEach(referenceConfig -> referenceConfig.destroy());
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        //
        Class referenceInterface = methodInvocation.getMethod().getDeclaringClass();
        ReferenceConfig referenceConfig = referenceConfigs.get(referenceInterface);
        if(referenceConfig==null) {
            synchronized (lock) {
                referenceConfig = referenceConfigs.get(referenceInterface);
                if(referenceConfig==null) {
                    referenceConfig = createReferenceConfig(configurableApplicationContext, referenceInterface);
                    referenceConfigs.put(referenceInterface, referenceConfig);
                }
            }
        }
        //
        return ReflectionUtils.invokeMethod(methodInvocation.getMethod(), referenceConfig.get(), methodInvocation.getArguments());
    }

    private Object invokeTargetMethod(MethodInvocation methodInvocation, Object target) {
        Class[] argumentTypes = new Class[methodInvocation.getArguments().length];
        for(int i=0; i<argumentTypes.length; i++) {
            argumentTypes[i] = methodInvocation.getArguments()[i].getClass();
        }
        //
        Method targetMethod = ReflectionUtils.findMethod(target.getClass(), methodInvocation.getMethod().getName(), argumentTypes);
        //
        return ReflectionUtils.invokeMethod(targetMethod, target, methodInvocation.getArguments());
    }

    private ReferenceConfig createReferenceConfig(ApplicationContext applicationContext, Class referenceInterface) throws Exception {
        DubboConfiguration dubboConfiguration = applicationContext.getBean(DubboConfiguration.class);
        Map<String, String> referenceParameters = dubboConfiguration.getReferenceParameters(referenceInterface.getName());
        //
        ReferenceBean<Object> referenceConfig = new ReferenceBean<>();
        referenceConfig.setScope(Constants.SCOPE_REMOTE);
        referenceConfig.setInterface(referenceInterface);
        //
        referenceConfig.setApplication(dubboConfiguration.getApplication());
        referenceConfig.setRegistry(dubboConfiguration.getRegistry());
        referenceConfig.setProtocol(dubboConfiguration.getProtocol().getName());
        //
        BeanWrapper referenceConfigWrapper = PropertyAccessorFactory.forBeanPropertyAccess(referenceConfig);
        referenceConfigWrapper.setPropertyValues(dubboConfiguration.getReferenceParameters(referenceInterface.getName()));
        //
        if (referenceParameters.containsKey("registry")) {
            List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
            for (String registryId : StringUtils.tokenizeToStringArray(referenceParameters.get("registry"), "|")) {
                if (registryId != null && registryId.length() > 0) {
                    registryConfigs.add(applicationContext.getBean(registryId, RegistryConfig.class));
                }
            }
            referenceConfig.setRegistries(registryConfigs);
        }
        if (referenceParameters.containsKey("consumer")) {
            referenceConfig.setConsumer((applicationContext.getBean(referenceParameters.get("consumer"), ConsumerConfig.class)));
        }
        if (referenceParameters.containsKey("monitor")) {
            referenceConfig.setMonitor(applicationContext.getBean(referenceParameters.get("monitor"), MonitorConfig.class));
        }
        if (referenceParameters.containsKey("application")) {
            referenceConfig.setApplication(applicationContext.getBean(referenceParameters.get("application"), ApplicationConfig.class));
        }
        if (referenceParameters.containsKey("module")) {
            referenceConfig.setModule(applicationContext.getBean(referenceParameters.get("module"), ModuleConfig.class));
        }
        //
        referenceConfig.afterPropertiesSet();
        //
        return referenceConfig;
    }

}
