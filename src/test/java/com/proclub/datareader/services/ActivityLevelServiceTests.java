package com.proclub.datareader.services;

import com.proclub.datareader.dao.ActivityLevel;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
public class ActivityLevelServiceTests {

    @Autowired
    private ActivityLevelService _service;


    @Test
    public void testCrud() {

        LocalDateTime trackDt = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        LocalDateTime modifiedDt = LocalDateTime.now();

        /*
            public ActivityLevel(UUID fkUserGuid, Instant modifiedDateTime, Instant trackDateTime, int fairlyActiveMinutes,
                         int lightlyActiveMinutes, int veryActiveMinutes, boolean deviceReported) {
         */
        ActivityLevel act1 = new ActivityLevel(TEST_USER_GUID1, modifiedDt, trackDt, TEST_FAIRLYACTIVE1, TEST_LIGHTLYACTIVE1,
                        TEST_VERYACTIVE1, true);

        act1 = _service.createActivityLevel(act1);
        assertNotNull(act1);
        assertTrue(act1.getActivityLevelId() > 0);

        act1.setFairlyActiveMinutes(TEST_FAIRLYACTIVE2);
        _service.updateActivityLevel(act1);

        Optional<ActivityLevel> opAct = _service.findById(act1.getActivityLevelId());
        assertTrue(opAct.isPresent());
        ActivityLevel act2 = opAct.get();
        assertEquals(act1, act2);

        act2 = new ActivityLevel(TEST_USER_GUID2, modifiedDt, trackDt, TEST_FAIRLYACTIVE2, TEST_LIGHTLYACTIVE2,
                TEST_VERYACTIVE2, true);

        act2 = _service.createActivityLevel(act2);
        assertNotNull(act2);
        assertTrue(act2.getActivityLevelId() > 0);
        assertNotEquals(act1, act2);

        // see if we have 2 in our list
        List<ActivityLevel> actList = _service.findAll();
        TestCase.assertEquals(2, actList.size());

        long count = _service.count();
        assertEquals(2, count);

        actList = _service.findByTrackDate(trackDt, trackDt.plus(1, ChronoUnit.DAYS));
        assertEquals(2, actList.size());

        _service.deleteActivityLevel(act1);
        count = _service.count();
        assertEquals(1, count);

        _service.deleteActivityLevel(act2);
        count = _service.count();
        assertEquals(0, count);

    }
}
