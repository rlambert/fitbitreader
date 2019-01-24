package com.proclub.datareader.model.weight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class WeightJsonTests {

    @Test
    public void testWeightResults() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "weightdata.json");
        WeightData wtData = mapper.readValue(json, WeightData.class);
        assertNotNull(wtData);

        SimpleTrack st = new SimpleTrack(wtData.getWeight().get(0));
        assertNotNull(st);

        Weight wt = wtData.getWeight().get(0);
        Instant dt1 = Instant.parse(wt.getDate() + "T" + wt.getTime() + ".00Z");
        int trackDateTime = (int) (dt1.toEpochMilli()/1000);
        assertTrue(trackDateTime > 0);
        Instant dt2 = Instant.ofEpochSecond(trackDateTime);
        assertEquals(dt1, dt2);
    }
}
