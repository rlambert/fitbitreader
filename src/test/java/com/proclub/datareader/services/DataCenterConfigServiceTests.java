package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.model.security.OAuthCredentials;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.proclub.datareader.TestConstants.*;
import static junit.framework.TestCase.assertTrue;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
@AutoConfigureTestDatabase
public class DataCenterConfigServiceTests {

    @Autowired
    private DataCenterConfigService _service;

    @Test
    public void testCredentials() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OAuthCredentials oauth = mapper.readValue(TEST_CREDS, OAuthCredentials.class);
        assertNotNull(oauth);
        assertEquals("e9400fba91a13f0f202593285c231f939fd8c6d418ab45c0e2601a431408566c", oauth.getRefreshToken());
    }

    @Test
    public void testCrud() {

        //    public DataCenterConfig(UUID fkUserGuid, int sourceSystem, Instant lastChecked, int panelDisplay, int status,
        // String statusText, int dataStatus, String credentials, LocalDateTime modified) {

        Instant lastChecked = Instant.now();
        Instant modified = Instant.now();
        DataCenterConfig dc1 = new DataCenterConfig(TEST_USER_GUID1, SOURCE_FITBIT, lastChecked,
                                        0, 0, "OK", 0, TEST_CREDS, modified);

        dc1 = _service.createDataCenterConfig(dc1);
        assertNotNull(dc1);

        DataCenterConfig.Pkey id = new DataCenterConfig.Pkey(dc1.getFkUserGuid(), dc1.getSourceSystem());
        Optional<DataCenterConfig> optDc = _service.findById(id);
        assertTrue(optDc.isPresent());

        DataCenterConfig dc2 = optDc.get();
        assertEquals(dc1, dc2);

        // now update
        dc2.setStatus(-1);
        _service.updateDataCenterConfig(dc2);

        // see if update worked
        optDc = _service.findById(id);
        dc2 = optDc.get();
        assertNotEquals(dc1, dc2);

        // see if we have 1 in our list
        List<DataCenterConfig> dcList = _service.findAll();
        assertTrue(dcList.size() == 1);

        // create another to test compound primary key
        dc2 = new DataCenterConfig(TEST_USER_GUID1, SOURCE_OTHER, lastChecked,
                0, 0, "OK", 0, TEST_CREDS, modified);

        dc2 = _service.createDataCenterConfig(dc2);

        id = new DataCenterConfig.Pkey(dc2.getFkUserGuid(), dc2.getSourceSystem());
        optDc = _service.findById(id);
        assertTrue(optDc.isPresent());

        long count = _service.count();
        assertEquals(2, count);

        // make sure we return 2
        dcList = _service.findAll();
        assertTrue(dcList.size() == 2);

        _service.deleteDataCenterConfig(dc2);
        count = _service.count();
        assertEquals(1, count);

        _service.deleteDataCenterConfig(dc1);
        count = _service.count();
        assertEquals(0, count);

    }
}
