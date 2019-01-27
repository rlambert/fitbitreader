/*
 -----------------------------------------
   FitBitController
   Copyright (c) 2019
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api.rest;

import com.proclub.datareader.api.ApiBase;
import com.proclub.datareader.model.steps.StepsData;
import com.proclub.datareader.services.FitBitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@RestController
@Component
@CrossOrigin
@RequestMapping("${app.apiRestBase}/process")
public class FitBitController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(FitBitController.class);

    private FitBitDataService _fitbitService;           // fitbit API service


    /**
     * constructor
     * @param fitBitDataService
     */
    public FitBitController(FitBitDataService fitBitDataService) {
        _fitbitService = fitBitDataService;
    }

    /**
     * process one user API
     * @param userId
     * @return
     * @throws IOException
     */

    @GetMapping(value = {"/{userid}/{notify}"}, produces = "application/json")
    public String getAll(@PathVariable UUID userId, @PathVariable boolean suppressNotifications) {
        try {
            _fitbitService.processUser(userId, suppressNotifications);
        }
        catch (IOException ex) {
            String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
            throw HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, msg, null, msg.getBytes(), null);
        }
        catch (IllegalArgumentException ex) {
            String msg = String.format("User %s does not exist in DataCenterConfig table.", userId);
            throw HttpClientErrorException.create(HttpStatus.NOT_FOUND, msg, null, msg.getBytes(), null);
        }
        return null;
    }

    @GetMapping(value = {"/steps/{userid}"}, produces = "application/json")
    public StepsData getSteps(@PathVariable UUID userId, @PathVariable boolean suppressNotifications) {
        try {
            return _fitbitService.getSteps(userId.toString(), suppressNotifications);
        }
        catch (IOException | InterruptedException | ExecutionException ex) {
            String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
            throw HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, msg, null, msg.getBytes(), null);
        }
        catch (IllegalArgumentException ex) {
            String msg = String.format("User %s does not exist in DataCenterConfig table.", userId);
            throw HttpClientErrorException.create(HttpStatus.NOT_FOUND, msg, null, msg.getBytes(), null);
        }
    }
}
