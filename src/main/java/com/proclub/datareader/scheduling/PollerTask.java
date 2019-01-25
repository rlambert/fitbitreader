package com.proclub.datareader.scheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.services.DataCenterConfigService;
import com.proclub.datareader.services.FitBitDataService;
import com.proclub.datareader.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PollerTask {

    private static Logger _logger = LoggerFactory.getLogger(PollerTask.class);

    private AppConfig _config;
    private DataCenterConfigService _dcService;
    private FitBitDataService _fitBitService;


    private ObjectMapper _mapper = new ObjectMapper();

    /**
     * constructor - params populated by Spring
     * @param config - AppConfig
     */
    @Autowired
    public PollerTask(AppConfig config, DataCenterConfigService dcService, FitBitDataService fbService) {
        _config = config;
        _dcService = dcService;
        _fitBitService = fbService;
    }

    /**
     * hourly task will fire at 5 minutes after the hour
     */
    // @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
    @Scheduled(cron = "${app.pollcron}")
    public void dataSourcePoller() {
        if ((!_config.isUnittest()) && (_config.isPollEnabled())) {
            LocalDateTime dt = LocalDateTime.now();
            _logger.info("*** Poller Task Started ****");
            _logger.info(dt.toString());
            _logger.info("");

            List<DataCenterConfig> subs = _dcService.findAll();

            for(DataCenterConfig dc : subs) {
                try {
                    _fitBitService.processAll(dc, dt);
                }
                catch (IOException ex) {
                    _logger.error(StringUtils.formatError(String.format("Error processing user: %s", dc.getFkUserGuid()), ex));
                }

            }

            _logger.info(String.format("Poll complete: %s total users processed.", subs.size()));
        }
    }

}
