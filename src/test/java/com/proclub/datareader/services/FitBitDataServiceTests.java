package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class FitBitDataServiceTests {

    @Autowired
    private FitBitDataService _service;

    @Test
    public void testWiring() {
        assertNotNull(_service);
    }

    // This only works if you snag a code from an OAuth login at FitBit API
    //@Test
    public void testAuth() throws Exception {
        OAuthCredentials credentials = _service.getAuth("80b08d8034f69aa1338b6b2dba9182f7b1cd3ae2");
        assertNotNull(credentials);
        assertEquals("22DFJK8", credentials.getAccessUserId());
    }

    // credentials.json must be updated with fresh credentials. This was just to test the
    // runtime code.
    @Test
    public void testSteps() throws Exception {
        final LocalDateTime dt = LocalDateTime.parse("2019-01-10T00:00:00");
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "credentials.json");
        OAuthCredentials creds = mapper.readValue(json, OAuthCredentials.class);
        assertNotNull(creds);

        json = StringUtils.readResource(this, "credentials2.json");
        creds = mapper.readValue(json, OAuthCredentials.class);
        assertNotNull(creds);

        json = mapper.writeValueAsString(creds);
        assertNotNull(json);

        /*
        DataCenterConfig dc = new DataCenterConfig();
        dc.setFkUserGuid(TEST_USER_GUID1);
        StepsData stepsData = _service.getSteps(dc, creds, dt);
        assertNotNull(stepsData);
        */
    }

}
