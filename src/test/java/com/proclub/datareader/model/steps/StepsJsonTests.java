package com.proclub.datareader.model.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StepsJsonTests {

    // just wanted to prove ConcurrentHashMap returns null
    // if the key is not in the map (as opposed to throwing
    // an exception) and that updates overwrite old values.
    @Test
    public void testConcurrentHashMap() {
        ConcurrentHashMap<UUID, OAuthCredentials> authMap = new ConcurrentHashMap<>();
        authMap.put(UUID.randomUUID(), new OAuthCredentials());

        UUID newUserId = UUID.randomUUID();
        OAuthCredentials creds = authMap.get(newUserId);
        assertNull(creds);

        OAuthCredentials creds1 = new OAuthCredentials();
        creds1.setRefreshToken("refresh1");
        authMap.put(newUserId, creds1);

        OAuthCredentials creds2 = new OAuthCredentials();
        creds2.setRefreshToken("refresh2");
        authMap.put(newUserId, creds2);

        OAuthCredentials creds3 = authMap.get(newUserId);
        assertEquals("refresh2", creds3.getRefreshToken());
    }


    @Test
    public void testSleepResults() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "stepsdata.json");
        StepsData stepsData = mapper.readValue(json, StepsData.class);
        assertNotNull(stepsData);

        //SimpleTrack st = new SimpleTrack(sleepData);
    }
}
