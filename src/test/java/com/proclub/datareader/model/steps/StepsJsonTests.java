package com.proclub.datareader.model.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.proclub.datareader.TestConstants.TEST_FKCLIENTID1;
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

    /**
     * helper to find a given Sleep instance in a list of
     * DB results for Sleep data
     * @param dbResults - List&lt;SimpleTrack&gt;
     * @param steps - Steps
     * @return Optional&lt;SimpleTrack&gt;
     */
    private Optional<SimpleTrack> findStepsMatch(Steps steps, List<SimpleTrack> dbResults) {
        String dtStr = steps.getDateTime();
        if (!dtStr.contains(":")) {
            dtStr += "T00:00:00.000";
        }
        LocalDateTime dt = LocalDateTime.parse(dtStr);
        ZoneOffset zos = ZoneOffset.ofHours(0);

        LocalDateTime dtDb;
        for (SimpleTrack item : dbResults) {
            dtDb = LocalDateTime.ofEpochSecond(item.getTrackDateTime(), 0, zos);
            if (dtDb.getDayOfYear() == dt.getDayOfYear()) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Test
    public void testStepsResults() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "stepsdata.json");
        StepsData stepsData = mapper.readValue(json, StepsData.class);
        assertNotNull(stepsData);
        List<SimpleTrack> dbList = new ArrayList<>();

        SimpleTrack st;
        for(Steps steps : stepsData.getSteps()) {
            st = new SimpleTrack(steps, TEST_FKCLIENTID1);
            dbList.add(st);
        }

        for(Steps steps : stepsData.getSteps()) {
            Optional<SimpleTrack> optSteps = findStepsMatch(steps, dbList);
            if (!optSteps.isPresent()) {
                throw new Exception("Couldn't find a match.");
            }
        }

    }
}
