/*
 -----------------------------------------
   TestController
   Copyright (c) 2018
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api.rest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.apis.fitbit.FitBitOAuth2AccessToken;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.proclub.datareader.api.ApiBase;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.*;
import com.proclub.datareader.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@RestController
@Component
@CrossOrigin
@RequestMapping("/admin")
public class TestController extends ApiBase {

    private static Logger _logger = LoggerFactory.getLogger(TestController.class);

    private AppConfig _config;                          // app configuration instance
    private UserService _userService;                   // our service to fetch user data
    private DataCenterConfigService _dcService;         // get DataCenterConfig table
    private ActivityLevelService _activityLevelService; // get/put ActivityLevel table
    private SimpleTrackService _trackService;           // get/put SimpleTrack table data
    private ClientService _clientService;               // gets Client table data
    private EmailService _emailService;                 // centralized email code

    public TestController(AppConfig config, UserService userService, DataCenterConfigService dcService,
                          ActivityLevelService activityLevelService, SimpleTrackService trackService,
                          ClientService clientService, EmailService emailService) {
        _config = config;
        _userService = userService;
        _dcService = dcService;
        _activityLevelService = activityLevelService;
        _trackService = trackService;
        _clientService = clientService;
        _emailService = emailService;
    }


    private void getAuth() throws InterruptedException, ExecutionException, IOException {
        // Replace these with your client id and secret fron your app
        final String clientId = "22DFJ8";
        final String clientSecret = "e212359c9f0945845dc744576f5d7789";
        final OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope("activity profile") // replace with desired scope
                //your callback URL to store and handle the authorization code sent by Fitbit
                .callback("https://proclub-fitbit-dev.azurewebsites.net/")
                .state("some_params")
                .build(FitbitApi20.instance());

        // Obtain the Authorization URL
        System.out.println("Fetching the Authorization URL...");
        final String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);

        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        //final String code = in.nextLine();
        //System.out.println();

        final String code = "03bf6923babf1936791a4979ca8ee7c197481979";

        // Trade the Request Token and Verfier for the Access Token
        System.out.println("Trading the Request Token for an Access Token...");
        final OAuth2AccessToken oauth2AccessToken = service.getAccessToken(code);
        System.out.println("Got the Access Token!");
        System.out.println("(if you're curious it looks like this: " + oauth2AccessToken
                + ", 'rawResponse'='" + oauth2AccessToken.getRawResponse() + "')");
        System.out.println();

        if (!(oauth2AccessToken instanceof FitBitOAuth2AccessToken)) {
            System.out.println("oauth2AccessToken is not instance of FitBitOAuth2AccessToken. Strange enough. exit.");
            return;
        }

        final FitBitOAuth2AccessToken accessToken = (FitBitOAuth2AccessToken) oauth2AccessToken;
        // Now let's go and ask for a protected resource!
        // This will get the profile for this user
        System.out.println("Now we're going to access a protected resource...");

        final OAuthRequest request = new OAuthRequest(Verb.GET,
                String.format("", accessToken.getUserId()));
        request.addHeader("x-li-format", "json");

        service.signRequest(accessToken, request);

        final Response response = service.execute(request);
        System.out.println();
        System.out.println(response.getCode());
        System.out.println(response.getBody());
    }

    private void headlessApproval() {
        String baseUrl = "https://www.fitbit.com/oauth2/authorize?response_type=code&client_id=22DFJ8&redirect_uri=https%3A%2F%2Fproclub-fitbit-dev.azurewebsites.net%2F&scope=activity%20sleep%20weight&expires_in=604800";
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        try {
            HtmlPage page = client.getPage(baseUrl);
            System.out.println(page.asXml());

            HtmlDivision div = page.getFirstByXPath("//div[@class='internal']");
            HtmlInput inputEmail = page.getFirstByXPath("//input[@tabindex='23']");

            HtmlInput inputPwd = page.getFirstByXPath("//input[@tabindex='24']");

            inputEmail.setValueAttribute("rosswlambert@gmail.com");
            inputPwd.setValueAttribute("RockNRollIn2019!");

            //get the enclosing form
            HtmlForm loginForm = inputPwd.getEnclosingForm() ;

            //submit the form
            client.getOptions().setJavaScriptEnabled(true);
            page = client.getPage(loginForm.getWebRequest(null));
            System.out.println(page.getWebResponse().getStatusCode());

            System.out.println("-------------------------");
            System.out.println(page.asXml());

            HtmlCheckBoxInput inputSelectAll = page.getFirstByXPath("//input[@id='selectAllScope']");
            inputSelectAll.setChecked(true);

            //get the enclosing form
            HtmlForm allowForm = inputSelectAll.getEnclosingForm();
            HtmlButton allowBtn = page.getFirstByXPath("//button[@id='allow-button']");

            page = client.getPage(allowForm.getWebRequest(allowBtn));

            System.out.println("-------------------------");
            System.out.println(page.asXml());
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * helper method to make sure this API is only available via localhost
     * @param req - HttpServletRequest
     * @throws HttpClientErrorException
     */
    private void checkHost(HttpServletRequest req) throws HttpClientErrorException {
        if ((!req.getRequestURL().toString().contains("localhost")) && (!req.getRequestURL().toString().contains("127.0.0.1"))) {
            throw HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Resource not available.", null, null, null);
        }
    }

    @GetMapping(value = {"test/full/{days:[\\d]+}"}, produces = "text/html")
    public String runTest(@PathVariable int days,  HttpServletRequest req) throws HttpClientErrorException {
        checkHost(req);

        return "OK";
    }

    @GetMapping(value = {"test/email/{toAddr}/{fname}"}, produces = "text/html")
    public String runMailTest(@PathVariable String toAddr, @PathVariable String fname, HttpServletRequest req) throws IOException {
        checkHost(req);
        _emailService.sendTemplatedEmail(toAddr, fname);
        return "Email sucessfully sent to: " + toAddr;
    }

    @GetMapping(value = {"test/db"}, produces = "application/json")
    public String runDbTest(HttpServletRequest req) throws IOException {
        checkHost(req);
        int count = _dcService.findAllFitbitActive().size();
        return "{\"totalActiveFitBitUsers\":\"" + count + "\"}";
    }

    @GetMapping(value = {"test/db/datacenterconfig/{id}/{system}"}, produces = "application/json")
    public DataCenterConfig runDbTestDc(@PathVariable UUID id, @PathVariable int system, HttpServletRequest req) throws IOException {
        checkHost(req);
        DataCenterConfig.Pkey key = new DataCenterConfig.Pkey(id, system);
        Optional<DataCenterConfig> opt = _dcService.findById(key);
        if (!opt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("{\"error\":\"DataCenterConfig ID: %s - %s does not exist.\"}", id, system));
        }
        else {
            return opt.get();
        }
    }

    @GetMapping(value = {"test/db/user/{id}"}, produces = "application/json")
    public User runDbTestUser(@PathVariable String id, HttpServletRequest req) throws IOException {
        checkHost(req);
        Optional<User> opt = _userService.findById(UUID.fromString(id));
        if (!opt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("{\"error\":\"User %s does not exist.\"}", id));
        }
        else {
            return opt.get();
        }
    }


    @GetMapping(value = {"test/db/user/email/{address}"}, produces = "application/json")
    public List<User> runDbTestUserEmail(@PathVariable String address, HttpServletRequest req) throws IOException {
        checkHost(req);
        List<User> users = _userService.findByEmail(address);
        return users;
    }


    @GetMapping(value = {"test/db/activitylevel/{id}"}, produces = "application/json")
    public ActivityLevel runDbTestActivityLevel(@PathVariable long id, HttpServletRequest req) throws IOException {
        checkHost(req);
        Optional<ActivityLevel> opt = _activityLevelService.findById(id);
        if (!opt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("{\"error\":\"ActivityLevel ID: %s does not exist.\"}", id));
        }
        else {
            return opt.get();
        }
    }

    @GetMapping(value = {"test/db/client/{id}"}, produces = "application/json")
    public Client runDbTestClient(@PathVariable int id, HttpServletRequest req) throws IOException {
        checkHost(req);
        Optional<Client> opt = _clientService.findById(id);
        if (!opt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("{\"error\":\"Client ID: %s does not exist.\"}", id));
        }
        else {
            return opt.get();
        }
    }

    @GetMapping(value = {"test/db/simpletrack/{id}"}, produces = "application/json")
    public SimpleTrack runDbTestTrack(@PathVariable UUID id, HttpServletRequest req) throws IOException {
        checkHost(req);
        Optional<SimpleTrack> opt = _trackService.findById(id);
        if (!opt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("{\"error\":\"SimpleTrack ID: %s does not exist.\"}", id));
        }
        else {
            return opt.get();
        }
    }


}
