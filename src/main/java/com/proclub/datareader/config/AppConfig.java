package com.proclub.datareader.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix="app")
@Validated
@Data
public class AppConfig {

    /**
     * Logger for this class.
     */
    private static final Logger _logger = LoggerFactory.getLogger(AppConfig.class.getName());

    private String apiRestBase;
    private String fitbitClientId;
    private String fitbitCodeAuth;
    private String fitbitClientSecret;
    private String fitbitScope;
    private String fitbitCallbackUrl;
    private String fitbitAuthUrl;
    private String fitbitTokenUrl;
    private String fitbitSleepUrl;
    private String fitbitWeightUrl;
    private String fitbitStepsUrl;
    private String fitbitActivityUrl;
    private String pollCron;
    private boolean fitbitSendAuthEmail;
    private boolean unittest;
    private String adminUser;
    private String adminPassword;

    private String authEmailSubject;
    private String authEmailFromAddr;
    private String authEmailTemplate;

}
