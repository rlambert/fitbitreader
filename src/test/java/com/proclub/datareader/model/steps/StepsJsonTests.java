package com.proclub.datareader.model.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StepsJsonTests {

    // just wanted to prove ConcurrentHashMap returns null
    // if the key is not in the map (as opposed to throwing
    // an exception) and that updates overwrite old values.
    @Test
    public void testConcurrentHashMap() {
        ConcurrentHashMap<UUID, OAuthCredentials> authMap = new ConcurrentHashMap<>();
        authMap.put(UUID.randomUUID(), new OAuthCredentials());

        UUID newUserId = UUID.randomUUID();
        OAuthCredentials creds = authMap.get(newUserId);
        assertNull(creds);

        OAuthCredentials creds1 = new OAuthCredentials();
        creds1.setRefreshToken("refresh1");
        authMap.put(newUserId, creds1);

        OAuthCredentials creds2 = new OAuthCredentials();
        creds2.setRefreshToken("refresh2");
        authMap.put(newUserId, creds2);

        OAuthCredentials creds3 = authMap.get(newUserId);
        assertEquals("refresh2", creds3.getRefreshToken());
    }


    @Test
    public void testDate() {

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDateTime dtNow = LocalDateTime.now();
        String dtStr = dtNow.format(DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println(dtStr);

        DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("MMM d, YYYY - hh:mm:ss a");
        dtStr = dtNow.format(fmt2);
        System.out.println(dtStr);

        dtStr = "2018-12-11T00:00:00.00Z";
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
