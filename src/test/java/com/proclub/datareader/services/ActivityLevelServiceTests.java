package com.proclub.datareader.services;

import com.proclub.datareader.dao.ActivityLevel;
import com.proclub.datareader.dao.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.proclub.datareader.TestConstants.*;
import static junit.framework.TestCase.assertTrue;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
//@AutoConfigureTestDatabase
public class ActivityLevelServiceTests {

    @Autowired
    private ActivityLevelService _service;

    @Autowired
    private UserService _userService;

    @Autowired
    JdbcTemplate jdbcTemplate;


    @Test
    public void testConnection() {
        Collection<Map<String, Object>> rows = jdbcTemplate.queryForList("select * from dbo.ActivityLevel;");
        assertNotNull(rows);
    }

    @Test
    public void testCrud() {

        LocalDateTime trackDt = LocalDateTime.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime modifiedDt = LocalDateTime.now();

        /*
            We need to create these users so that the FK relationship works
         */
        int ts = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));

        User user1 = new User(/*TEST_USER_GUID1,*/ ts, TEST_CLIENT_TYPE1, TEST_EMAIL1,
                TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE1, TEST_FKCLIENTID1);

        user1 = _userService.createUser(user1);

        User user2 = new User(/*TEST_USER_GUID2, */ ts, TEST_CLIENT_TYPE2, TEST_EMAIL2,
                TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE2, TEST_FKCLIENTID2);

        user2 = _userService.createUser(user2);

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
        assertEquals(act1.getActivityLevelId(), act2.getActivityLevelId());
        assertEquals(act1.getFairlyActiveMinutes(), act2.getFairlyActiveMinutes());

        act2 = new ActivityLevel(TEST_USER_GUID2, modifiedDt, trackDt, TEST_FAIRLYACTIVE2, TEST_LIGHTLYACTIVE2,
                TEST_VERYACTIVE2, true);

        act2 = _service.createActivityLevel(act2);
        assertNotNull(act2);
        assertTrue(act2.getActivityLevelId() > 0);
        assertNotEquals(act1.getActivityLevelId(), act2.getActivityLevelId());
        assertNotEquals(act1.getFkUserGuid(), act2.getFkUserGuid());

        // see if we have 2 in our list
        List<ActivityLevel> actList = _service.findAll();
        assertTrue(actList.size() >= 2);

        long count = _service.count();
        assertTrue(actList.size() >= 2);

        // when debugging it handy to have a look at all
        actList = _service.findAll();
        assertTrue(actList.size() > 0);

        actList = _service.findByTrackDate(trackDt, trackDt.plus(1, ChronoUnit.DAYS));
        assertTrue(actList.size() >= 2);

        actList = _service.findAll();
        for (ActivityLevel item : actList) {
            _service.deleteActivityLevel(item);
        }
        count = _service.count();
        assertEquals(0, count);

        _userService.deleteUser(user1);
        _userService.deleteUser(user2);

    }
}
