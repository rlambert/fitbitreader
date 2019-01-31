/*
 -----------------------------------------
   TestController
   Copyright (c) 2018
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.error;

import com.proclub.datareader.api.ApiBase;
import com.proclub.datareader.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;


@RestController
@Component
@CrossOrigin
@RequestMapping("/error")
public class ErrorController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(ErrorController.class);
    private static TimeZone _timeZone = Calendar.getInstance().getTimeZone();

    private AppConfig _config;                          // app configuration instance


    public ErrorController(AppConfig config) {
        _config = config;
    }


    @GetMapping(value = {"/",""}, produces = "text/html")
    public String showError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return this.generateJsonView(req, genMessage("Error", String.format("There has been an error. HTTP Response code: %s, %s",
                resp.getStatus(), HttpStatus.resolve(resp.getStatus()).getReasonPhrase())));
    }

}
