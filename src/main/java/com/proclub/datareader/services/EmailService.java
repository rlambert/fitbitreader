package com.proclub.datareader.services;

import com.proclub.datareader.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class EmailService {

    private static Logger _logger = LoggerFactory.getLogger(EmailService.class);

    private JavaMailSender _sender;

    /**
     * Spring will provide a JavaMailSender instance since the Spring Boot
     * starter dependency already includes and registers the relevant beans
     * @param sender - JavaMailSender
     */
    public EmailService(JavaMailSender sender) {
        _sender = sender;
    }

    /**
     * sends an email
     * @param toAddr - String
     * @param fromAddr - String
     * @param ccAddr - String
     * @param bccAddr - String
     * @param subject - String
     * @param body - String
     */
    public void sendMessage(String toAddr, String fromAddr, String ccAddr, String bccAddr, String subject, String body) {

        try {
            MimeMessage message = _sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
            helper.setText(body, true);  // is HTML

            helper.setTo(toAddr);
            helper.setFrom(fromAddr);

            if (!StringUtils.isNullOrEmpty(subject)) {
                helper.setSubject(subject);
            }
            if (!StringUtils.isNullOrEmpty(ccAddr)) {
                helper.setCc(ccAddr);
            }
            if (!StringUtils.isNullOrEmpty(bccAddr)) {
                helper.setCc(bccAddr);
            }
            _sender.send(message);
        }
        catch (MessagingException ex) {
            _logger.error(StringUtils.formatError(ex));
        }
    }

    public void sendMessage(String toAddr, String fromAddr, String subject, String body) {
        sendMessage(toAddr, fromAddr, null, null, subject, body);
    }
}
