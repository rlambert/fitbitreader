package com.proclub.datareader.services;

import com.proclub.datareader.dao.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.proclub.datareader.TestConstants.*;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserService _service;

    @Test
    public void testExisting() {
        UUID userId = UUID.fromString("EB711738-5011-47FC-BFA3-365B2FE8848C");
        Optional<User> optUser = _service.findById(userId.toString());
        assertTrue(optUser.isPresent());
    }

    @Test
    public void test1User() {

        int ts = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));

        User user1 = new User(ts, TEST_CLIENT_TYPE1, TEST_EMAIL1,
                TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE1, TEST_FKCLIENTID1);

        user1 = _service.createUser(user1);

        User user2 = new User(ts, TEST_CLIENT_TYPE2, TEST_EMAIL2,
                TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE2, TEST_FKCLIENTID2);

        user2 = _service.createUser(user2);

        Optional<User> optUser = _service.findById(user1.getUserGuid());
        assertTrue(optUser.isPresent());
        User user3 = optUser.get();
        assertEquals(user1, user3);

        optUser = _service.findById(user2.getUserGuid());
        assertTrue(optUser.isPresent());
        user3 = optUser.get();
        assertEquals(user2, user3);
    }

    @Test
    public void testCrud() {

        int ts = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));

        // User(UUID userGuid, int modifiedDateTime, int clientType, String email, String fkUserStore2020,
        // String fkUserStorePRO, String postalCode, String fkClientId) {
        //User user1a = new User(TEST_USER_GUID1, ts, TEST_CLIENT_TYPE1, TEST_EMAIL1,
        //        TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE1, TEST_FKCLIENTID1);

        User user1a = new User(ts, TEST_CLIENT_TYPE1, TEST_EMAIL1,
                TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE1, TEST_FKCLIENTID1);

        user1a = _service.createUser(user1a);

        Optional<User> optUser1 = _service.findById(user1a.getUserGuid());
        assertTrue(optUser1.isPresent());
        User user1b = optUser1.get();
        assertEquals(user1a, user1b);

        user1a.setPostalCode(TEST_POSTAL_CODE2);
        _service.updateUser(user1a);
        optUser1 = _service.findById(user1a.getUserGuid());
        assertTrue(optUser1.isPresent());
        user1b = optUser1.get();
        assertEquals(TEST_POSTAL_CODE2, user1b.getPostalCode());

        User user2 = new User(/* TEST_USER_GUID2,*/ ts, TEST_CLIENT_TYPE2, TEST_EMAIL2,
                TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE2, TEST_FKCLIENTID2);

        _service.createUser(user2);

        long count = _service.count();
        assertEquals(2, count);

        List<User> users = _service.findByEmail(TEST_EMAIL1);
        assertTrue(users.size() == 1);

        users = _service.findByModifiedDateTimeAfter(ts - 100);
        assertEquals(2, users.size());

        _service.deleteUser(user1a);
        count = _service.count();
        assertEquals(1, count);

        _service.deleteUser(user2);
        count = _service.count();
        assertEquals(0, count);
    }
}
