package com.proclub.datareader.services;

import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailService {

    private static Logger _logger = LoggerFactory.getLogger(EmailService.class);

    private JavaMailSender _sender;
    private AppConfig _config;

    /**
     * Spring will provide a JavaMailSender instance since the Spring Boot
     * starter dependency already includes and registers the relevant beans
     * @param sender - JavaMailSender
     * @param config - AppConfig
     */
    public EmailService(JavaMailSender sender, AppConfig config) {
        _sender = sender;
        _config = config;
    }

    /**
     * sends an HTML email
     * @param toAddr - String
     * @param fromAddr - String
     * @param ccAddr - String
     * @param bccAddr - String
     * @param subject - String
     * @param body - String
     */
    public void sendMessage(String toAddr, String fromAddr, String ccAddr, String bccAddr, String subject, String body)
            throws MailException, MessagingException {

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
        catch (MailException | MessagingException ex) {
            _logger.error(StringUtils.formatError(ex));
            throw ex;
        }
    }

    /**
     * overload with no CC or CC (for brevity)
     * @param toAddr - String
     * @param fromAddr - String
     * @param subject - String
     * @param body - String
     */
    public void sendMessage(String toAddr, String fromAddr, String subject, String body) throws MessagingException {
        sendMessage(toAddr, fromAddr, null, null, subject, body);
    }

    /**
     * a cheap and sleazy but enormously effective template engine.
     * @param paramMap - Map&lt;String, String&ht;
     * @return String
     * @throws IOException
     */
    private String createEmailBody(Map<String, String> paramMap) throws IOException {
        String tmpl = StringUtils.readResource(this, _config.getAuthEmailTemplate());

        for(Map.Entry entry : paramMap.entrySet())
        {
            tmpl = tmpl.replace(entry.getKey().toString(), entry.getValue().toString());
        }
        return tmpl;
    }

    /**
     * grabs template from config and sends the email
     * @param toAddr
     * @param fname
     * @throws IOException
     */
    public void sendTemplatedEmail(String toAddr, String fname) throws IOException, MessagingException {
        String subject = _config.getAuthEmailSubject();
        String fromAddr = _config.getAuthEmailFromAddr();
        Map<String, String> map = new HashMap<>();
        map.put("{fname}", fname);

        String htmlBody = createEmailBody(map);
        sendMessage(toAddr, fromAddr, subject, htmlBody);
    }
}
