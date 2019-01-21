package com.proclub.datareader.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class EmailServiceTest {

    @Autowired
    EmailService _emailService;


    @Test
    public void testEmail() {
        // public void sendMessage(String toAddr, String fromAddr, String ccAddr, String bccAddr, String subject, String body) {

        String toAddr = "rlambert@bpcs.com";
        String fromAddr = "rosswlambert@gmail.com";
        String subject = "test email subject";
        String body = "<h1>This is a big headline</h1><p>This is a paragraph.</p>";

        _emailService.sendMessage(toAddr, fromAddr, subject, body);
    }

}
