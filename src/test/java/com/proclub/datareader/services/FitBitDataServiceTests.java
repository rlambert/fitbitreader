package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.model.steps.StepsData;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
@AutoConfigureTestDatabase
public class FitBitDataServiceTests {

    @Autowired
    private FitBitDataService _service;

    @Test
    public void testAuth() throws Exception {
        OAuthCredentials credentials = _service.getAuth("80b08d8034f69aa1338b6b2dba9182f7b1cd3ae2");
        assertNotNull(credentials);
        assertEquals("22DFJK8", credentials.getAccessUserId());
    }

    @Test
    public void testSteps() throws Exception {
        final Instant dt = Instant.parse("2019-01-10T00:00:00Z");
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "credentials.json");
        OAuthCredentials creds = mapper.readValue(json, OAuthCredentials.class);
        assertNotNull(creds);

        StepsData stepsData = _service.getSteps(creds, dt);
        assertNotNull(stepsData);

    }

}
