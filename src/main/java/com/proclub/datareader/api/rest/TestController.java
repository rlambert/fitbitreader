/*
 -----------------------------------------
   TestController
   Copyright (c) 2018
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api.rest;

import com.proclub.datareader.api.ApiBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@RestController
@Component
@CrossOrigin
@RequestMapping("/admin")
public class TestController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(TestController.class);


    @GetMapping(value = {"test/full/{days}"}, produces = "text/html")
    public String runTest(@PathVariable String days,  HttpServletRequest req) throws IOException {
        if ((!req.getRequestURL().toString().contains("localhost")) && (!req.getRequestURL().toString().contains("127.0.0.1"))) {
            throw HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Resource not available.", null, null, null);
        }

        return "OK";
    }
}
