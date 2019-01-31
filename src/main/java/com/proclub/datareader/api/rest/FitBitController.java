/*
 -----------------------------------------
   FitBitController
   Copyright (c) 2019
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.api.ApiBase;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.AuditLog;
import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.services.AuditLogService;
import com.proclub.datareader.services.DataCenterConfigService;
import com.proclub.datareader.services.FitBitDataService;
import com.proclub.datareader.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@Component
@CrossOrigin
@RequestMapping("/userdata")
public class FitBitController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(FitBitController.class);

    private FitBitDataService _fitbitService;           // fitbit API service
    private AppConfig _config;
    private DataCenterConfigService _dcService;
    private AuditLogService _auditService;

    private ObjectMapper _mapper = new ObjectMapper();

    /**
     * constructor - params populated by Spring
     * @param config - AppConfig
     */
    @Autowired
    public FitBitController(AppConfig config, DataCenterConfigService dcService,
                            FitBitDataService fbService, AuditLogService auditService) {
        _config = config;
        _dcService = dcService;
        _fitbitService = fbService;
        _auditService = auditService;
    }

    @GetMapping(value = {"/process/{notify}"}, produces = "text/html")
    public String processAll(@PathVariable boolean notify, HttpServletRequest req) throws JsonProcessingException {
        LocalDateTime dtEnd = LocalDateTime.now();
        LocalDateTime dtMidnight = LocalDateTime.of(dtEnd.getYear(), dtEnd.getMonth(), dtEnd.getDayOfMonth(), 0, 0, 0, 0);
        // go back in time a configurable number of days
        LocalDateTime dtStart = dtMidnight.minusDays(_config.getFitbitQueryWindow());

        List<DataCenterConfig> subs = _dcService.findAllFitbitActive();

        LocalDateTime dtProcessStart = LocalDateTime.now();
        for(DataCenterConfig dc : subs) {
            try {
                _fitbitService.processAll(dc, dtStart, dtEnd, notify);
            }
            catch (IOException ex) {
                _logger.error(StringUtils.formatError(String.format("Error processing user: %s", dc.getFkUserGuid()), ex));
            }
        }
        LocalDateTime dtProcessEnd = LocalDateTime.now();
        Duration duration = Duration.between(dtProcessStart, dtProcessEnd);

        String details = String.format("Polling complete for %s total users. Elapsed time: %s minutes", subs.size(), duration.get(ChronoUnit.MINUTES));
        AuditLog log = new AuditLog("00000000-0000-0000-0000-000000000000", LocalDateTime.now(), AuditLog.Activity.RunSummary, details);
        _auditService.createOrUpdate(log);

        _logger.info(String.format("Poll complete: %s total users processed.", subs.size()));
        return genMessage("Processing Result", details);
    }

    @GetMapping(value = {"/process/batch/{batch}/{dtStartStr}/{dtEndStr}/{suppressNotifications}"}, produces = "text/html")
    public String getBatch(@PathVariable String batch, @PathVariable String dtStartStr,
                           @PathVariable String dtEndStr, @PathVariable boolean suppressNotifications, HttpServletRequest req)
            throws IOException {

        Map<String, String> result = new HashMap<>();

        String[] users = batch.split(",");
        for(String userId : users) {
            try {
                LocalDateTime dtEnd = LocalDateTime.parse(StringUtils.fixDateTimeStr(dtEndStr));
                LocalDateTime dtStart = LocalDateTime.parse(StringUtils.fixDateTimeStr(dtStartStr));
                _fitbitService.processUser(UUID.fromString(userId), dtStart, dtEnd, suppressNotifications);
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
