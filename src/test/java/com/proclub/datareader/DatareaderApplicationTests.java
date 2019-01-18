package com.proclub.datareader;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.proclub.datareader.config.AppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class DatareaderApplicationTests {

    @Autowired
    AppConfig _config;

    @Test
    public void contextLoads() {
        assertNotNull(_config);
        System.out.println("This will error out if Spring cannot load all beans or encounters some other error.");
    }

    @Test
    public void testCronConfig() {
        String cronExpr = _config.getPollCron();
        assertNotNull(cronExpr);
    }

    @Test
    public void testUserApproval() {
        String baseUrl = "https://www.fitbit.com/oauth2/authorize?response_type=code&client_id=22DFJ8&redirect_uri=https%3A%2F%2Fproclub-fitbit-dev.azurewebsites.net%2F&scope=activity%20sleep%20weight&expires_in=604800";
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        try {
            HtmlPage page = client.getPage(baseUrl);
            System.out.println(page.asXml());

            HtmlDivision div = page.getFirstByXPath("//div[@class='internal']");
            assertNotNull(div);
            HtmlInput inputEmail = page.getFirstByXPath("//input[@tabindex='23']");
            assertNotNull(inputEmail);

            HtmlInput inputPwd = page.getFirstByXPath("//input[@tabindex='24']");
            assertNotNull(inputPwd);

            inputEmail.setValueAttribute("rosswlambert@gmail.com");
            inputPwd.setValueAttribute("RockNRollIn2019!");

            //get the enclosing form
            HtmlForm loginForm = inputPwd.getEnclosingForm() ;

            //submit the form
            client.getOptions().setJavaScriptEnabled(true);
            page = client.getPage(loginForm.getWebRequest(null));
            System.out.println(page.getWebResponse().getStatusCode());
            assertNotNull(page);
            System.out.println("-------------------------");
            System.out.println(page.asXml());

            HtmlCheckBoxInput inputSelectAll = page.getFirstByXPath("//input[@id='selectAllScope']");
            inputSelectAll.setChecked(true);

            //get the enclosing form
            HtmlForm allowForm = inputSelectAll.getEnclosingForm();
            HtmlButton allowBtn = page.getFirstByXPath("//button[@id='allow-button']");

            page = client.getPage(allowForm.getWebRequest(allowBtn));
            assertNotNull(page);

            System.out.println("-------------------------");
            System.out.println(page.asXml());
        }
        catch(Exception ex){
                ex.printStackTrace();
        }
    }

}

