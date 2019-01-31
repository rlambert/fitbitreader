/*
 -----------------------------------------
   TestController
   Copyright (c) 2018
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.proclub.datareader.api.ApiBase;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.AuditLog;
import com.proclub.datareader.services.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


@RestController
@Component
@CrossOrigin
@RequestMapping("/audit")
public class AuditLogController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(AuditLogController.class);
    private static TimeZone _timeZone = Calendar.getInstance().getTimeZone();

    private AppConfig _config;                          // app configuration instance
    private AuditLogService _logService;                // our log service


    public AuditLogController(AppConfig config, AuditLogService logService) {
        _config = config;
        _logService = logService;
    }



    /**
     * helper method to make sure this API is only available via localhost
     *
     * @param req - HttpServletRequest
     * @throws HttpClientErrorException
     */
    private void checkHost(HttpServletRequest req) throws HttpClientErrorException {
        if ((!req.getRequestURL().toString().contains("localhost")) && (!req.getRequestURL().toString().contains("127.0.0.1"))) {
            throw HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Resource not available.", null, null, null);
        }
    }

//    @GetMapping(value = {"/{animal}"}, produces = "text/html")
//    public String getLog3(@PathVariable String animal, HttpServletRequest req) throws IOException {
//        checkHost(req);
//        Map<String, String> messages = new HashMap<>();
//        messages.put("animal", animal);
//        return this.generateJsonView(req, this.serialize(messages));
//    }

    @GetMapping(value = {"/events/{dtStr}"}, produces = "text/html")
    public String getLog2(@PathVariable String dtStr, HttpServletRequest req) throws IOException {
        checkHost(req);
        try {
            dtStr += "T00:00:00.000";
            LocalDateTime dtStart = LocalDateTime.parse(dtStr);
            List<AuditLog> log = _logService.findByDateTimeAfter(dtStart);
            return this.generateJsonView(req, this.serialize(log));
        }
        catch (JsonProcessingException ex) {
            return this.generateJsonView(req, this.serialize(String.format("{\"JSON generation error\":\"%s\"}", ex.getMessage())));
        }
        catch (IOException ex) {
            return this.generateJsonView(req, this.serialize(String.format("{\"IOException\":\"%s\"}", ex.getMessage())));
        }
    }

    @GetMapping(value = {"/", ""}, produces = "text/html")
    public String getLog(HttpServletRequest req) throws IOException {
        checkHost(req);
        Map<String, String> messages = new HashMap<>();
        messages.put("Usage: get events by user and start date", "/{userId}/{dtStart}");
        messages.put("Usage: get events by start date", "/{dtStart} --> like 2019-01-31");
        return this.generateJsonView(req, this.serialize(messages));
    }

    @GetMapping(value = {"/events/{userId}/{dtStart}"}, produces = "text/html")
    public String getLog(@PathVariable String userId, @PathVariable LocalDateTime dtStart, HttpServletRequest req) throws IOException {
        checkHost(req);
        try {
            List<AuditLog> log = _logService.findByUserAndDateTime(userId, dtStart);
            return this.generateJsonView(req, this.serialize(log));
        }
        catch (JsonProcessingException ex) {
            return this.generateJsonView(req, this.serialize(String.format("{\"JSON generation error\":\"%s\"}", ex.getMessage())));
        }
        catch (IOException ex) {
            return this.generateJsonView(req, this.serialize(String.format("{\"IOException\":\"%s\"}", ex.getMessage())));
        }
    }



}
