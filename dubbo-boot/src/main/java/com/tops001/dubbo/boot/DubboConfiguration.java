package com.tops001.dubbo.boot;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo Configuration
 *
 * @author Gavin Hu
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "dubbo")
public class DubboConfiguration {

    private String[] servicePackages;

    private String[] referencePackages;

    private String[] services;

    private String[] references;

    private ApplicationConfig application;

    private RegistryConfig registry;

    private ProtocolConfig protocol;

    private Map<String, Map<String, String>> serviceParametersMapping = new HashMap<>();

    private Map<String, Map<String, String>> referenceParametersMapping = new HashMap<>();

    public String[] getServicePackages() {
        return servicePackages;
    }

    public void setServicePackages(String[] servicePackages) {
        this.servicePackages = servicePackages;
    }

    public String[] getReferencePackages() {
        return referencePackages;
    }

    public void setReferencePackages(String[] referencePackages) {
        this.referencePackages = referencePackages;
    }

    public String[] getServices() {
        return services;
    }

    public void setServices(String[] services) {
        this.services = services;
    }

    public String[] getReferences() {
        return references;
    }

    public void setReferences(String[] references) {
        this.references = references;
    }

    public ApplicationConfig getApplication() {
        return application;
    }

    public void setApplication(ApplicationConfig application) {
        this.application = application;
    }

    public RegistryConfig getRegistry() {
        return registry;
    }

    public void setRegistry(RegistryConfig registry) {
        this.registry = registry;
    }

    public ProtocolConfig getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolConfig protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getServiceParameters(String serviceName) {
        if (serviceParametersMapping.isEmpty() && services!=null) {
            for (String service : services) {
                resolveMapping(serviceParametersMapping, service);
            }
        }
        //
        return serviceParametersMapping.containsKey(serviceName) ?
                serviceParametersMapping.get(serviceName) : new HashMap<>();
    }

    public Map<String, String> getReferenceParameters(String referenceName) {
        if(referenceParametersMapping.isEmpty() && references!=null) {
            for (String reference : references) {
                resolveMapping(referenceParametersMapping, reference);
            }
        }
        //
        return referenceParametersMapping.containsKey(referenceName) ?
                referenceParametersMapping.get(referenceName) : new HashMap<>();
    }

    private void resolveMapping(Map<String, Map<String, String>> mapping, String target) {
        Map<String, String> targetParametersMap = new HashMap<>();
        String[] targetParts = StringUtils.split(target, "=");
        String targetInterface = targetParts[0];
        String[] targetParameters = StringUtils.tokenizeToStringArray(targetParts[1], "&");
        //
        for (String targetParameter : targetParameters) {
            String[] targetParameterPair = StringUtils.split(targetParameter, ":");
            targetParametersMap.put(targetParameterPair[0], targetParameterPair[1]);
        }
        //
        mapping.put(targetInterface, targetParametersMap);
    }

}