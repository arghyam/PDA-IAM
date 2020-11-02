package com.pda.backend.config;


import com.pda.backend.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TraceIdPostProcessor implements EnvironmentPostProcessor, ApplicationRunner {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TraceIdPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        LOGGER.info("Before processing the Trace Id");
        Map<String, Object> map = new HashMap<>();
        if (Boolean.parseBoolean(environment.getProperty("tracing.enabled", "true"))) {
            map.put("logging.pattern.level", "%5p [X-TRACE-ID: %X{logLevelPattern}]");
        }
        map.put("spring.aop.proxyTargetClass", "true");
        appendOrWrite(environment.getPropertySources(), map);
    }

    private void appendOrWrite(MutablePropertySources propertySources, Map<String, Object> map) {
        MapPropertySource target = null;
        if (propertySources.contains(Constants.PROPERTY_SOURCE_NAME)  && propertySources.get(Constants.PROPERTY_SOURCE_NAME) instanceof MapPropertySource) {
            PropertySource<?> source = propertySources.get(Constants.PROPERTY_SOURCE_NAME);
            target = (MapPropertySource) source;
            for (String key : map.keySet()) {
                if (!target.containsProperty(key)) {
                    target.getSource().put(key, map.get(key));
                }
            }

        }
        if (target == null) {
            target = new MapPropertySource(Constants.PROPERTY_SOURCE_NAME, map);
        }
        if (!propertySources.contains(Constants.PROPERTY_SOURCE_NAME)) {
            propertySources.addLast(target);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info(Arrays.toString(args.getSourceArgs()));
    }
}
