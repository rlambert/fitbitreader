package com.proclub.datareader.model.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;

public class StepsJsonTests {

    @Test
    public void testDate() {
        String dtStr = "2018-12-11T00:00:00.00Z";
        //LocalDate dt1 = LocalDate.parse(dtStr);
        Instant dt1 = Instant.parse(dtStr);
        int dtval = (int) (dt1.toEpochMilli()/1000);

        Instant dt2 = Instant.ofEpochSecond(dtval);
        assertEquals(dt1, dt2);

        LocalDateTime locDt = LocalDateTime.ofInstant(dt1, ZoneOffset.UTC);
        dtStr = locDt.format(DateTimeFormatter.ISO_DATE);
        System.out.println(dtStr);

        LocalDateTime dt = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        System.out.println(dt);
        assertEquals(0, dt.getMinute());

        // 1/16/2019 4:17:40 PM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy hh:mm:ss a");
        dt    = LocalDateTime.now();
        dt    = dt.plus(300000, ChronoUnit.MILLIS);
        dtStr = dt.format(formatter);
        System.out.println(dtStr);

    }

    @Test
    public void testSleepResults() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "stepsdata.json");
        StepsData stepsData = mapper.readValue(json, StepsData.class);
        assertNotNull(stepsData);

        //SimpleTrack st = new SimpleTrack(sleepData);
    }
}
