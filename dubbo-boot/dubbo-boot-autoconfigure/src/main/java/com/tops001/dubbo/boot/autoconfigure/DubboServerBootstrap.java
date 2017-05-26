package com.tops001.dubbo.boot.autoconfigure;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.spring.ServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dubbo Server Bootstrap
 *
 * @author Gavin Hu
 * @version 1.0.0
 */
public class DubboServerBootstrap implements ApplicationListener<ApplicationContextEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DubboServerBootstrap.class);

    private ConfigurableApplicationContext configurableApplicationContext;

    private Set<ServiceConfig> serviceConfigs = new HashSet<>();

    private MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    public DubboServerBootstrap(ConfigurableApplicationContext configurableApplicationContext) {
        this.configurableApplicationContext = configurableApplicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent applicationContextEvent) {
        if(applicationContextEvent instanceof ContextRefreshedEvent) {
            try {
                exportServices(applicationContextEvent.getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(applicationContextEvent instanceof ContextClosedEvent) {
            try {
                unexportServices(applicationContextEvent.getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void unexportServices(ApplicationContext applicationContext) throws Exception {
        serviceConfigs.forEach(serviceConfig -> {
            serviceConfig.unexport();
            //
            logger.info("Unexported service interface [{}]", serviceConfig.getInterface());
        });
    }

    // @see com.alibaba.dubbo.config.spring.AnnotationBean
    public void exportServices(ApplicationContext applicationContext) throws Exception {
        //
        DubboConfiguration dubboConfiguration = applicationContext.getBean(DubboConfiguration.class);
        //
        if(dubboConfiguration.getServicePackages()!=null) {
            for (String servicePackage : dubboConfiguration.getServicePackages()) {
                String scanPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(servicePackage) + "/**/*.class";
                Resource[] resources = applicationContext.getResources(scanPattern);
                for (Resource resource : resources) {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    if (metadataReader.getClassMetadata().isInterface()) {
                        //
                        Class serviceInterface = applicationContext.getClassLoader().loadClass(metadataReader.getClassMetadata().getClassName());
                        Object serviceImplementation = applicationContext.getBean(serviceInterface);
                        //
                        ServiceConfig serviceConfig = createServiceBean(applicationContext, serviceInterface, serviceImplementation);
                        //
                        serviceConfigs.add(serviceConfig);
                        //
                        serviceConfig.export();
                        //
                        logger.info("Exported service interface [{}]", serviceConfig.getInterface());
                    }
                }
            }
        }
    }

    private ServiceBean createServiceBean(ApplicationContext applicationContext, Class serviceInterface, Object serviceImplementation) throws Exception {
        //
        DubboConfiguration dubboConfiguration = applicationContext.getBean(DubboConfiguration.class);
        Map<String, String> serviceParameters = dubboConfiguration.getServiceParameters(serviceInterface.getName());
        //
        ServiceBean<Object> serviceConfig = new ServiceBean<>();
        serviceConfig.setApplication(dubboConfiguration.getApplication());
        serviceConfig.setRegistry(dubboConfiguration.getRegistry());
        serviceConfig.setProtocol(dubboConfiguration.getProtocol());
        //
        BeanWrapper serviceConfigWrapper = PropertyAccessorFactory.forBeanPropertyAccess(serviceConfig);
        serviceConfigWrapper.setPropertyValues(serviceParameters);
        //
        serviceConfig.setInterface(serviceInterface);
        if (serviceParameters.containsKey("registry")) {
            List<RegistryConfig> registryConfigs = new ArrayList<>();
            for (String registryId : StringUtils.tokenizeToStringArray(serviceParameters.get("registry"), "|")) {
                registryConfigs.add(applicationContext.getBean(registryId, RegistryConfig.class));
            }
            serviceConfig.setRegistries(registryConfigs);
        }
        if (serviceParameters.containsKey("provider")) {
            serviceConfig.setProvider((applicationContext.getBean(serviceParameters.get("provider"), ProviderConfig.class)));
        }
        if (serviceParameters.containsKey("monitor")) {
            serviceConfig.setMonitor(applicationContext.getBean(serviceParameters.get("monitor"), MonitorConfig.class));
        }
        if (serviceParameters.containsKey("application")) {
            serviceConfig.setApplication(applicationContext.getBean(serviceParameters.get("application"), ApplicationConfig.class));
        }
        if (serviceParameters.containsKey("module")) {
            serviceConfig.setModule(applicationContext.getBean(serviceParameters.get("module"), ModuleConfig.class));
        }
        if (serviceParameters.containsKey("protocol")) {
            List<ProtocolConfig> protocolConfigs = new ArrayList<ProtocolConfig>();
            // modified by lishen; fix dubbo's bug
            for (String protocolId : StringUtils.tokenizeToStringArray(serviceParameters.get("protocol"), "|")) {
                if (protocolId != null && protocolId.length() > 0) {
                    protocolConfigs.add(applicationContext.getBean(protocolId, ProtocolConfig.class));
                }
            }
            serviceConfig.setProtocols(protocolConfigs);
        }
        //
        serviceConfig.setRef(serviceImplementation);
        //
        serviceConfig.afterPropertiesSet();
        //
        return serviceConfig;
    }

}
