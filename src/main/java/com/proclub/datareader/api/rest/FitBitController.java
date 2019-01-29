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
import com.proclub.datareader.model.sleep.SleepData;
import com.proclub.datareader.model.steps.StepsData;
import com.proclub.datareader.model.weight.WeightData;
import com.proclub.datareader.services.FitBitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@RestController
@Component
@CrossOrigin
@RequestMapping("/userdata")
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

    @GetMapping(value = {"/{userid}/{notify}"}, produces = "text/html")
    public String getAll(@PathVariable UUID userId, @PathVariable boolean suppressNotifications, HttpServletRequest req)
            throws IOException {
        try {
            _fitbitService.processUser(userId, suppressNotifications);
            return this.generateJsonView(req, genMessage("result", String.format("User %s successfully processed", userId.toString())));
        }
        catch (IOException ex) {
            String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
            return this.generateJsonView(req, genMessage("error", msg));
        }
        catch (IllegalArgumentException ex) {
            String msg = String.format("User %s does not exist in DataCenterConfig table.", userId);
            return this.generateJsonView(req, genMessage("error", msg));
        }
    }

    @GetMapping(value = {"/steps/{userid}"}, produces = "text/html")
    public String getSteps(@PathVariable UUID userId, @PathVariable boolean suppressNotifications,
                           HttpServletRequest req) throws IOException {
        try {
            StepsData data = _fitbitService.getSteps(userId.toString(), suppressNotifications);
            return this.generateJsonView(req, this.serialize(data));
        }
        catch (IOException | InterruptedException | ExecutionException ex) {
            String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
            return this.generateJsonView(req, genMessage("error", msg));
        }
        catch (IllegalArgumentException ex) {
            String msg = String.format("User %s does not exist in DataCenterConfig table.", userId);
            return this.generateJsonView(req, genMessage("error", msg));
        }
    }

    @GetMapping(value = {"/weight/{userid}"}, produces = "text/html")
    public String getWeight(@PathVariable UUID userId, @PathVariable boolean suppressNotifications,
                           HttpServletRequest req) throws IOException {
        try {
            WeightData data = _fitbitService.getWeight(userId.toString(), suppressNotifications);
            return this.generateJsonView(req, this.serialize(data));
        }
        catch (IOException | InterruptedException | ExecutionException ex) {
            String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
            return this.generateJsonView(req, genMessage("error", msg));
        }
        catch (IllegalArgumentException ex) {
            String msg = String.format("User %s does not exist in DataCenterConfig table.", userId);
            return this.generateJsonView(req, genMessage("error", msg));
        }
    }

    @GetMapping(value = {"/sleep/{userid}"}, produces = "text/html")
    public String getSleep(@PathVariable UUID userId, @PathVariable boolean suppressNotifications,
                            HttpServletRequest req) throws IOException {
        try {
            SleepData data = _fitbitService.getSleep(userId.toString(), suppressNotifications);
            return this.generateJsonView(req, this.serialize(data));
        }
        catch (IOException | InterruptedException | ExecutionException ex) {
            String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
            return this.generateJsonView(req, genMessage("error", msg));
        }
        catch (IllegalArgumentException ex) {
            String msg = String.format("User %s does not exist in DataCenterConfig table.", userId);
            return this.generateJsonView(req, genMessage("error", msg));
        }
    }

    @GetMapping(value = {"/process/{batch}"}, produces = "text/html")
    public String getBatch(@PathVariable String batch, HttpServletRequest req)
            throws IOException {

        Map<String, String> result = new HashMap<>();

        String[] users = batch.split(",");
        for(String userId : users) {
            try {
                _fitbitService.processUser(UUID.fromString(userId), true);
                result.put("result", String.format("%s successfully processed", userId));
            }
            catch (IOException ex) {
                String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
                result.put("result", msg);
            }
            catch (IllegalArgumentException ex) {
                String msg = String.format("User %s does not exist in DataCenterConfig table.", userId);
                result.put("result", msg);
            }
            catch(Exception ex) {
                String msg = String.format("Error processing user %s, error: %s", userId, ex.getMessage());
                result.put("result", msg);
            }
        }
        return this.generateJsonView(req, this.serialize(result));
    }
}
