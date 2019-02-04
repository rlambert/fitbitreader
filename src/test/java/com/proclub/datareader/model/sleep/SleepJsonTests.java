package com.proclub.datareader.model.sleep;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.io.IOException;

import static com.proclub.datareader.TestConstants.TEST_USER_GUID1;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class SleepJsonTests {

    @Test
    public void testSleepResults() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "sleepdata2.json");
        SleepData sleepData = mapper.readValue(json, SleepData.class);
        assertNotNull(sleepData);

        SimpleTrack st = null;
        for(Sleep sleep : sleepData.getSleep()) {
            st = new SimpleTrack(sleep, TEST_USER_GUID1.toString());
        }
        assertNotNull(st);
    }
}
