package com.proclub.datareader.services;

import com.proclub.datareader.dao.AuditLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.proclub.datareader.TestConstants.TEST_USER_GUID1;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class AuditLogServiceTests {

    @Autowired
    private AuditLogService _service;


    @Test
    public void testCrud() throws IOException {

        LocalDateTime dtNow = LocalDateTime.now();
        AuditLog log1 = new AuditLog(TEST_USER_GUID1.toString(), dtNow, AuditLog.Activity.RefreshedCredentials, "These are test credentials");
        log1 = _service.createOrUpdate(log1);

        Optional<AuditLog> optLog = _service.findById(log1.getId());
        assertTrue(optLog.isPresent());
        AuditLog log2 = optLog.get();
        assertEquals(log1, log2);

        log1.setDetails("New details");
        _service.createOrUpdate(log1);

        optLog = _service.findById(log1.getId());
        assertTrue(optLog.isPresent());
        log2 = optLog.get();
        assertEquals(log1, log2);

        List<AuditLog> logs = _service.findByDateTimeAfter(dtNow.minusDays(1));
        assertTrue(logs.size() > 0);

        logs = _service.findByUserAndDateTime(TEST_USER_GUID1.toString(), dtNow.minusDays(1));
        assertTrue(logs.size() > 0);
    }
}
