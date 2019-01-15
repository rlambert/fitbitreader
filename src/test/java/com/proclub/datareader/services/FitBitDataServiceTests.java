package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
@AutoConfigureTestDatabase
public class FitBitDataServiceTests {

    @Autowired
    private FitBitDataService _service;


    @Test
    public void testSteps() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "credentials.json");
        OAuthCredentials creds = mapper.readValue(json, OAuthCredentials.class);
        assertNotNull(creds);
        //_service.getSteps()
    }
}
