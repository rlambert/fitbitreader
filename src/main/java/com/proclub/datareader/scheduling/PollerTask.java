package com.proclub.datareader.scheduling;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PollerTask {

    private static Logger _logger = LoggerFactory.getLogger(PollerTask.class);

    private AppConfig _config;
    private DataCenterConfigService _dcService;
    private FitBitDataService _fitBitService;
    private AuditLogService _auditService;


    /**
     * constructor - params populated by Spring
     * @param config - AppConfig
     */
    @Autowired
    public PollerTask(AppConfig config, DataCenterConfigService dcService, FitBitDataService fbService, AuditLogService auditService) {
        _config = config;
        _dcService = dcService;
        _fitBitService = fbService;
        _auditService = auditService;
    }


    /**
     * hourly task will fire at 5 minutes after the hour
     */
    // @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
    @Scheduled(cron = "${app.pollcron}")
    public void dataSourcePoller() {

        LocalDateTime dtEnd = LocalDateTime.now();

        /// we do not do any processing if it is unit test or if
        // polling is turned off completely via configuration

        if (!_config.isUnittest()) {
            if ((_config.isPollEnabled())) {

                _logger.info("**** Poller Task Started ****");
                _logger.info("   Start time: " + dtEnd.toString());
                _logger.info("");

                LocalDateTime dtMidnight = LocalDateTime.of(dtEnd.getYear(), dtEnd.getMonth(), dtEnd.getDayOfMonth(), 0, 0, 0, 0);
                // go back in time a configurable number of days
                LocalDateTime dtStart = dtMidnight.minusDays(_config.getFitbitQueryWindow());

                List<DataCenterConfig> subs = _dcService.findAllFitbitActive();

                LocalDateTime dtProcessStart = LocalDateTime.now();
                for (DataCenterConfig dc : subs) {
                    try {
                        _fitBitService.processAll(dc, dtStart, dtEnd);
                    }
                    catch (Exception ex) {
                        _logger.error(StringUtils.formatError(String.format("Error processing user: %s", dc.getFkUserGuid()), ex));
                        if (ex instanceof NullPointerException) {
                            AuditLog log = new AuditLog(AuditLogService.systemUserGuid, LocalDateTime.now(), AuditLog.Activity.CredentialsError, ex.getMessage());
                            _auditService.createOrUpdate(log);
                        }
                    }

                }
                LocalDateTime dtProcessEnd = LocalDateTime.now();
                Duration duration = Duration.between(dtProcessStart, dtProcessEnd);

                String details = String.format("Polling complete for %s total users. Elapsed time: %s minutes", subs.size(), (duration.getSeconds() / 60));
                AuditLog log = new AuditLog(AuditLogService.systemUserGuid, LocalDateTime.now(), AuditLog.Activity.RunSummary, details);
                _auditService.createOrUpdate(log);

                _logger.info(String.format("Poll complete: %s total users processed.", subs.size()));
            }
            else {
                // polling is disabled, so we are going to just make a note
                // and exit
                String msg = String.format("Polling is disabled via configuration, no action taken at %s", dtEnd);
                _logger.info(msg);
                AuditLog log = new AuditLog(AuditLogService.systemUserGuid, LocalDateTime.now(), AuditLog.Activity.RunNotice, msg);
                _auditService.createOrUpdate(log);
            }
        }
    }

}
