/*
 -----------------------------------------
   VersionController
   Copyright (c) 2018
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api.rest;

import com.proclub.datareader.api.ApiBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;


@RestController
@Component
@CrossOrigin
@RequestMapping("${app.apiRestBase}/version")
public class VersionController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(VersionController.class);

    @Value("${spring.application.name}")
    private String _appName;

    private Environment _environment;


    @Value("${app.version}")
    private String _version;

    @Autowired
    public VersionController(Environment environment) {
        _environment = environment;
    }

    private String getIpAddress() {
        InetAddress ip;
        String addy;

        try {
            ip = InetAddress.getLocalHost();
            addy = ip.getHostAddress();
        }
        catch (UnknownHostException e) {
            addy = "Unknown Host";
        }
        return addy;
    }

    private String getVersion() throws IOException {
        String profileTxt = "default";

        // extract all active profiles
        String[] profiles = _environment.getActiveProfiles();
        if ((profiles != null) && (profiles.length > 0)) {
            StringBuilder sb = new StringBuilder();
            for (String profile : profiles) {
                sb.append(profile);
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
            profileTxt = sb.toString();
        }
        Properties props = new Properties();
        props.put("active.profiles", profileTxt);
        props.put("appname", _appName);
        props.put("version", _version);

        return serialize(props);
    }

    @GetMapping(value = {"","/"}, produces = "application/json")
    public String version1() throws IOException {
        return getVersion();
    }
}
