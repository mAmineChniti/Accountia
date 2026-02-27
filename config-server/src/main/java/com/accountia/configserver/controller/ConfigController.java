package com.accountia.configserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    @GetMapping("/{application}/{profile}")
    public Map<String, Object> getConfig(@PathVariable String application, @PathVariable String profile) {
        try {
            Environment env = environmentRepository.findOne(application, profile, "main");
            Map<String, Object> response = new HashMap<>();
            response.put("application", application);
            response.put("profile", profile);
            response.put("propertySources", env.getPropertySources());
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Configuration not found");
            error.put("application", application);
            error.put("profile", profile);
            return error;
        }
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "config-server");
        return status;
    }
}
