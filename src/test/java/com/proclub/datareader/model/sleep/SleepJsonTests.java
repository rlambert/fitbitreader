package com.proclub.datareader.model.sleep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.io.IOException;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class SleepJsonTests {

    @Test
    public void testSleepResults() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "sleepdata.json");
        SleepData sleepData = mapper.readValue(json, SleepData.class);
        assertNotNull(sleepData);

        SimpleTrack st = new SimpleTrack(sleepData);
        assertNotNull(st);
    }
}
