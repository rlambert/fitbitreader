package com.proclub.datareader;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateTests {

    @Test
    public void testDateParsing() {

        /*
            {
      "bmi": 28.49,
      "date": "2019-01-08",
      "logId": 1546991999000,
      "source": "API",
      "time": "23:59:59",
      "weight": 87.5
    }
         */
        LocalDateTime dt = LocalDateTime.parse("2019-01-08T23:59:59.000");
        System.out.println(dt.toString());

        String dstr = "1/21/2019 5:18:59 PM";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");
        LocalDateTime dt2 = LocalDateTime.parse(dstr, fmt);
        assertNotNull(dt2);
        assertEquals(21, dt2.getDayOfMonth());

        dstr = "10/9/2019 10:18:59 PM";
        dt2 = LocalDateTime.parse(dstr, fmt);
        assertNotNull(dt2);
        assertEquals(9, dt2.getDayOfMonth());

        String dtStr = "2018-12-11";
        //         String dtStr = "2018-12-11T00:00:00.00Z";
        if (!dtStr.contains(":")) {
            dtStr += "T00:00:00.000";
        }
        //LocalDateTime dtTrack = LocalDateTime.parse(dtStr);
        //DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("yyyy-MM-ddThh:mm:ssZ");
        //DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("yyyy-MM-ddThh:mm:ssZ");

        //LocalDateTime dt = LocalDateTime.parse("2019-01-08T23:59:59.000");
        //System.out.println(dt.toString());

        LocalDateTime dtTrack = LocalDateTime.parse(dtStr);
        System.out.println(dtTrack);
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
}
