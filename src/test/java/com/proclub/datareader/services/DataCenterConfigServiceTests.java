package com.proclub.datareader.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.model.security.OAuthCredentials;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.proclub.datareader.TestConstants.*;
import static com.proclub.datareader.dao.DataCenterConfig.PartnerStatus.Active;
import static junit.framework.TestCase.assertTrue;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class DataCenterConfigServiceTests {

    @Autowired
    private DataCenterConfigService _service;

    ObjectMapper _mapper = new ObjectMapper();

    @Test
    public void testDcDates() throws JsonProcessingException {
        LocalDateTime dt = LocalDateTime.now();
        LocalDateTime modified = LocalDateTime.now();
        DataCenterConfig dc1 = new DataCenterConfig(TEST_USER_GUID1.toString(), SimpleTrack.SourceSystem.FITBIT.sourceSystem, dt,
                0, Active.status, "OK", 0, TEST_CREDS, modified);
        dc1.setStatus(DataCenterConfig.PartnerStatus.Active.status);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, YYYY - hh:mm:ss a");
        dc1.setStatusText(dt.format(fmt));
        dc1.setLastChecked(dt);
        String json = _mapper.writeValueAsString(dc1);
        assertNotNull(json);
    }

    @Test
    public void testCredentials() throws Exception {
        OAuthCredentials oauth = _mapper.readValue(TEST_CREDS, OAuthCredentials.class);
        assertNotNull(oauth);
        assertEquals("e9400fba91a13f0f202593285c231f939fd8c6d418ab45c0e2601a431408566c", oauth.getRefreshToken());
    }

    @Test
    public void testJson() throws Exception {
        LocalDateTime lastChecked = LocalDateTime.now();
        LocalDateTime modified = LocalDateTime.now();
        DataCenterConfig dc1 = new DataCenterConfig(TEST_USER_GUID1.toString(), SimpleTrack.SourceSystem.FITBIT.sourceSystem, lastChecked,
                0, Active.status, "OK", 0, TEST_CREDS, modified);

        String json = _mapper.writeValueAsString(dc1);
        assertNotNull(json);

//        DataCenterConfig dc2 = _mapper.readValue(json, DataCenterConfig.class);
//        assertNotNull(dc2);
//        assertEquals(dc1, dc2);

        dc1.setCredentials("");
        dc1.setOAuthCredentials(null);

        json = _mapper.writeValueAsString(dc1);
        assertNotNull(json);

//        dc2 = _mapper.readValue(json, DataCenterConfig.class);
//        assertNotNull(dc2);
//        assertEquals(dc1, dc2);

        dc1.setCredentials(null);
        json = _mapper.writeValueAsString(dc1);
        assertNotNull(json);

//        dc2 = _mapper.readValue(json, DataCenterConfig.class);
//        assertNotNull(dc2);
//        assertEquals(dc1, dc2);
    }

    @Test
    public void testCrud() {

        //    public DataCenterConfig(UUID fkUserGuid, int sourceSystem, Instant lastChecked, int panelDisplay, int status,
        // String statusText, int dataStatus, String credentials, LocalDateTime modified) {

        LocalDateTime lastChecked = LocalDateTime.now();
        LocalDateTime modified = LocalDateTime.now();
        DataCenterConfig dc1 = new DataCenterConfig(TEST_USER_GUID1.toString(), SimpleTrack.SourceSystem.FITBIT.sourceSystem, lastChecked,
                                        0, Active.status, "OK", 0, TEST_CREDS, modified);

        dc1 = _service.createDataCenterConfig(dc1);
        assertNotNull(dc1);

        Optional<DataCenterConfig> optDc = _service.findById(dc1.getFkUserGuid(), dc1.getSourceSystem());
        assertTrue(optDc.isPresent());

        DataCenterConfig dc2 = optDc.get();
        assertEquals(dc1, dc2);

        // now update
        dc2.setStatus(-1);
        _service.updateDataCenterConfig(dc2);

        // see if update worked
        optDc = _service.findById(dc1.getFkUserGuid(), dc1.getSourceSystem());
        dc2 = optDc.get();
        assertNotEquals(dc1, dc2);

        // see if we have at least 1 in our list
        List<DataCenterConfig> dcList = _service.findAll();
        assertTrue(dcList.size() > 0);

        // create another to test compound primary key
        dc2 = new DataCenterConfig(TEST_USER_GUID1.toString(), SimpleTrack.SourceSystem.GARMIN.sourceSystem, lastChecked,
                0, Active.status, "OK", 0, TEST_CREDS, modified);

        dc2 = _service.createDataCenterConfig(dc2);

        optDc = _service.findById(dc2.getFkUserGuid(), dc2.getSourceSystem());
        assertTrue(optDc.isPresent());

        long count = _service.count();
        assertTrue(count > 1);

        // make sure we return 2
        dcList = _service.findAll();
        assertTrue(dcList.size() > 1);

        /*
        // restore active flag
        optDc = _service.findById(dc1.getFkUserGuid(), dc1.getSourceSystem());
        assertTrue(optDc.isPresent());
        dc1 = optDc.get();
        dc1.setStatus(DataCenterConfig.PartnerStatus.Active.status);
        _service.updateDataCenterConfig(dc1);

        optDc = _service.findById(dc1.getFkUserGuid(), dc1.getSourceSystem());
        assertTrue(optDc.isPresent());
        dc1 = optDc.get();
        assertEquals(DataCenterConfig.PartnerStatus.Active.status, dc1.getStatus());
        */
        DataCenterConfig dc3 = new DataCenterConfig(TEST_USER_GUID2.toString(), SimpleTrack.SourceSystem.FITBIT.sourceSystem, lastChecked,
                0, Active.status, "OK", 0, TEST_CREDS, modified);
        _service.createDataCenterConfig(dc3);

        // check new active filter
        dcList = _service.findAllFitbitActive();
        assertTrue(dcList.size() > 1);

        /*
        dcList = _service.findAll();
        for (DataCenterConfig dc : dcList) {
            _service.deleteDataCenterConfig(dc);
        }
        count = _service.count();
        assertEquals(0, count);
        */
    }
}
