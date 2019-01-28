package com.proclub.datareader;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.dao.User;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.services.DataCenterConfigService;
import com.proclub.datareader.services.FitBitDataService;
import com.proclub.datareader.services.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.proclub.datareader.TestConstants.*;
import static com.proclub.datareader.dao.DataCenterConfig.PartnerStatus.Active;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ActiveProfiles("unittest")
@SpringBootTest
public class DatareaderApplicationTests {

    @Autowired
    AppConfig _config;
    @Autowired
    private FitBitDataService _fbService;
    @Autowired
    private DataCenterConfigService _dcService;
    @Autowired
    private UserService _userService;


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

        final String clientId = _config.getFitbitClientId();
        final String clientSecret = _config.getFitbitClientSecret();

        final OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope(_config.getFitbitScope())
                //your callback URL to store and handle the authorization code sent by Fitbit
                .callback(_config.getFitbitCallbackUrl())
                .build(FitbitApi20.instance());

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authUrl = service.getAuthorizationUrl();

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        try {
            HtmlPage page = client.getPage(authUrl);
            System.out.println(page.asXml());

            HtmlDivision div = page.getFirstByXPath("//div[@class='internal']");
            assertNotNull(div);
            HtmlInput inputEmail = page.getFirstByXPath("//input[@tabindex='23']");
            assertNotNull(inputEmail);

            HtmlInput inputPwd = page.getFirstByXPath("//input[@tabindex='24']");
            assertNotNull(inputPwd);

            inputEmail.setValueAttribute(_config.getFitbitTestUser());
            inputPwd.setValueAttribute(_config.getFitbitTestPassword());

            //get the enclosing form
            HtmlForm loginForm = inputPwd.getEnclosingForm() ;

            //submit the form
            client.getOptions().setJavaScriptEnabled(true);
            page = client.getPage(loginForm.getWebRequest(null));
            System.out.println("FORM SUBMITTED");
            System.out.println("New page URL: " + page.getBaseURL());
            System.out.println(page.getWebResponse().getStatusCode());

            if (!page.getBaseURL().toString().contains(_config.getFitbitCallbackUrl())) {
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

            String qs = page.getBaseURL().getQuery();
            String code = qs.split("=")[1];


            int ts = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));

            User user;
            List<User> users = _userService.findByEmail(_config.getFitbitTestUser());
            if (users.size() > 0) {
                user = users.get(0);
            }
            else {
                user = new User(ts, TEST_CLIENT_TYPE1, _config.getFitbitTestUser(), TEST_FKUSERTORE2020, TEST_FKUSERSTOREPRO, TEST_POSTAL_CODE1, TEST_FKCLIENTID1);
                user = _userService.createUser(user);
            }

            OAuthCredentials creds = _fbService.getAuth(code);
            LocalDateTime lastChecked = LocalDateTime.now();
            LocalDateTime modified = LocalDateTime.now();
            String json = creds.toJson();


            DataCenterConfig dc;
            Optional<DataCenterConfig> optDc = _dcService.findById(user.getUserGuid(), SimpleTrack.SourceSystem.FITBIT.sourceSystem);
            if (optDc.isPresent()) {
                dc = optDc.get();
                dc.setOAuthCredentials(creds);
                dc.setCredentials(json);
                _dcService.updateDataCenterConfig(dc);
                //creds = dc.getOAuthCredentials();
            }
            else {
                dc = new DataCenterConfig(user.getUserGuid(), SimpleTrack.SourceSystem.FITBIT.sourceSystem, lastChecked,
                        0, Active.status, "OK", 0, json, modified);
                dc = _dcService.createDataCenterConfig(dc);
            }

            LocalDateTime dtStart = lastChecked.minusMinutes(5);

            long startTs = Instant.now().toEpochMilli();
            _fbService.processAll(dc, dtStart);
            long endTs = Instant.now().toEpochMilli();
            System.out.println(String.format("Total time for process all: %s", endTs - startTs));


//            StepsData stepsData = _fbService.getSteps(dc, dtStart);
//            System.out.println(stepsData);

//            SleepData sleepData = _fbService.getSleep(dc, dtStart);
//            System.out.println(sleepData);
//
//            WeightData weightData = _fbService.getWeight(dc, dtStart);
//            System.out.println(weightData);

//            List<ActivityLevelData> activityLevelList = _fbService.getActivityLevels(dc, dtStart);
//            System.out.println(activityLevelList);

        }
        catch(Exception ex){
                ex.printStackTrace();
        }
    }

}

