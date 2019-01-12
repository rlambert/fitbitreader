package com.proclub.datareader.services;

import com.proclub.datareader.dao.UserSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static com.proclub.datareader.TestConstants.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
@AutoConfigureTestDatabase
public class UserSessionServiceTests {

    @Autowired
    private UserSessionService _service;

    @Test
    public void testCrud() {

//    public UserSession(UUID sessionToken, int rememberUntil, String databaseVersion, int sessionTokenExpiresDate,
//      UUID fkUserGuid, UUID fkDeviceGuid) {

        // we convert to seconds from millis to account for Unix timestamp in DB being
        // in seconds from epoch

        UserSession session1 = new UserSession(TEST_SESSION_TOKEN1, REMEMBER_UNTIL1, "1.0",
                                TOKEN1_EXPIRES, TEST_USER_GUID1, TEST_DEVICE_GUID1);

        // create
        _service.createUserSession(session1);

        Optional<UserSession> optSession = _service.findById(TEST_SESSION_TOKEN1);
        assertTrue(optSession.isPresent());
        UserSession session2 = optSession.get();
        assertEquals(session1, session2);

        // update

        session1.setFkDeviceGuid(TEST_DEVICE_GUID2);
        _service.updateSession(session1);

        optSession = _service.findById(TEST_SESSION_TOKEN1);
        assertTrue(optSession.isPresent());
        session2 = optSession.get();
        assertEquals(TEST_DEVICE_GUID2, session2.getFkDeviceGuid());

        // add a second session

        session2 = new UserSession(TEST_SESSION_TOKEN2, REMEMBER_UNTIL2, "1.0",
                TOKEN2_EXPIRES, TEST_USER_GUID2, TEST_DEVICE_GUID2);

        session2 = _service.createUserSession(session2);

        long count = _service.count();
        assertEquals(2, count);

        List<UserSession> sessions = _service.findAll(Sort.by("SessionTokenExpiresDate"));
        assertEquals(2, sessions.size());

        // delete

        _service.deleteUserSession(session1);
        count = _service.count();
        assertEquals(1, count);

        _service.deleteUserSession(session2);
        count = _service.count();
        assertEquals(0, count);

    }
}
