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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@Component
@CrossOrigin
@RequestMapping("${app.apiRestBase}")
public class FitBitController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(FitBitController.class);

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String version;

    @Autowired
    public FitBitController(){ }

    @GetMapping(value = {"/activitylevel/{userid}/"}, produces = "application/json")
    public String getSleep(@PathVariable String userId) throws IOException {
        return null;
    }
}
