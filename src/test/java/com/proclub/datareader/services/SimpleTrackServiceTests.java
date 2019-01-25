package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.model.sleep.SleepData;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class SimpleTrackServiceTests {

    @Autowired
    private SimpleTrackService _service;


    @Test
    public void testCrud() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "sleepdata.json");
        SleepData sleepData = mapper.readValue(json, SleepData.class);
        assertNotNull(sleepData);
        SimpleTrack st = new SimpleTrack(sleepData.getSleep().get(0));
        assertNotNull(st);

        st = _service.createSimpleTrack(st);
        assertNotNull(st.getSimpleTrackGuid());

        Optional<SimpleTrack> optSt = _service.findById(st.getSimpleTrackGuid());
        assertTrue(optSt.isPresent());
        SimpleTrack st2 = optSt.get();
        assertEquals(st, st2);
    }
}
