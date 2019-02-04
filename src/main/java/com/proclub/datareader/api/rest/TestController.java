/*
 -----------------------------------------
   TestController
   Copyright (c) 2018
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.proclub.datareader.api.ApiBase;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.*;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.model.steps.StepsData;
import com.proclub.datareader.services.*;
import com.proclub.datareader.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.proclub.datareader.dao.DataCenterConfig.PartnerStatus.Active;


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
    private FitBitDataService _fitbitService;           // fitbit API service
    private AuditLogService _auditService;

    private JdbcTemplate _jdbcTemplate;


    public TestController(AppConfig config, UserService userService, DataCenterConfigService dcService,
                          ActivityLevelService activityLevelService, SimpleTrackService trackService,
                          ClientService clientService, EmailService emailService, FitBitDataService fitBitDataService,
                          AuditLogService auditLogService, JdbcTemplate jdbcTemplate) {
        _config = config;
        _userService = userService;
        _dcService = dcService;
        _activityLevelService = activityLevelService;
        _trackService = trackService;
        _clientService = clientService;
        _emailService = emailService;
        _fitbitService = fitBitDataService;
         _auditService = auditLogService;
        _jdbcTemplate = jdbcTemplate;
    }



    /**
     * helper method to generate a JSON response from an Optional
     * @param opt
     * @param type
     * @param id
     * @return
     * @throws JsonProcessingException
     */
    private String genResponse(Optional opt, String type, String id, HttpServletRequest req) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (!opt.isPresent()) {
            String msg = String.format("error - %s ID: %s does not exist.}", type, id);
            result.put("result", msg);
        } else {
            result.put("result", opt.get());
        }
        return this.generateJsonView(req, this.serialize(result));
    }

    /**
     * helper method to make sure this API is only available via localhost
     *
     * @param req - HttpServletRequest
     * @throws HttpClientErrorException
     */
    private void checkHost(HttpServletRequest req) throws HttpClientErrorException {
        if ((!req.getRequestURL().toString().contains("localhost")) && (!req.getRequestURL().toString().contains("127.0.0.1"))) {
            throw HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Resource not available.", null, null, null);
        }
    }

    @GetMapping(value = {"timezone"}, produces = "text/html")
    public String showTz(HttpServletRequest req) throws IOException {
        TimeZone tz = FitBitDataService._timeZone;
        return this.generateJsonView(req, genMessage("Server Timezone", tz.getDisplayName()));
     }

    @GetMapping(value = {"test/auth"}, produces = "text/html")
    public String testAuth(HttpServletRequest req) throws IOException {

        List<DataCenterConfig> subs = _dcService.findAllFitbitActive();
        List<String> results = new ArrayList<>();

        for(DataCenterConfig dc : subs) {
            try {
                if (!_fitbitService.preFlightOAuth(dc)) {
                    results.add(dc.getFkUserGuid());
                }
            }
            catch (Exception ex) {
                _logger.error(StringUtils.formatError(String.format("Error processing user: %s", dc.getFkUserGuid()), ex));
            }
        }

        String details = String.format("Auth pre-flight check for %s total users. Total who would receive emails: %s", subs.size(), results.size());
        AuditLog log = new AuditLog(AuditLogService.systemUserGuid, LocalDateTime.now(), AuditLog.Activity.RunSummary, details);
        _auditService.createOrUpdate(log);
        _logger.info(details);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("Summary", details);
        resultMap.put("UserList", results);
        return this.generateJsonView(req, this.serialize(resultMap));
    }

    @GetMapping(value = {"test/error"}, produces = "text/html")
    public String testError() {
        throw HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "This is a test error.", null, null, null);
    }

    @GetMapping(value = {"test/email/{toAddr}/{fname}"}, produces = "text/html")
    public String runMailTest(@PathVariable String toAddr, @PathVariable String fname, HttpServletRequest req) throws IOException {
        checkHost(req);
        Map<String, String> result = new HashMap<>();
        try {
            _emailService.sendTemplatedEmail(toAddr, fname);
            result.put("result", "Email sucessfully sent to: " + toAddr);
        }
        catch(MessagingException ex) {
            result.put("result", String.format("Error sending to: %s, %s", toAddr, ex.getMessage()));
        }
        catch(Exception ex) {
            result.put("result", String.format("Error sending to: %s, %s", toAddr, ex.getMessage()));
        }

        return this.generateJsonView(req, this.serialize(result));
    }

    @GetMapping(value = {"test/db"}, produces = "text/html")
    public String runDbTest(HttpServletRequest req) throws IOException {
        checkHost(req);
        try {
            int count = _dcService.findAllFitbitActive().size();
            String json = "{\"totalActiveFitBitUsers\":\"" + count + "\"}";
            return this.generateJsonView(req, json);
        }
        catch (IOException ex) {
            return this.generateJsonView(req, String.format("{\"error\":\"s%\"}", ex.getMessage()));
        }
    }

    @GetMapping(value = {"test/db/datacenterconfig/{id}/{system}"}, produces = "text/html")
    public String runDbTestDc(@PathVariable UUID id, @PathVariable int system, HttpServletRequest req) throws IOException {
        checkHost(req);
        try {
            Optional<DataCenterConfig> opt = _dcService.findById(id.toString(), system);
            return genResponse(opt, "DataCenterConfig", id.toString(), req);
        }
        catch (IOException ex) {
            return this.generateJsonView(req, String.format("{\"error\":\"s%\"}", ex.getMessage()));
        }

    }

    @GetMapping(value = {"test/db/datacenterconfig/active"}, produces = "text/html")
    public String runDbTestActiveDc(HttpServletRequest req) throws IOException {
        checkHost(req);
        try {
            return this.generateJsonView(req, this.serialize(_dcService.findAllFitbitActive()));
        }
        catch (Exception ex) {
            _logger.error(StringUtils.formatError(ex));
            return this.generateJsonView(req, this.genMessage("error", ex.getMessage()));
        }
    }


    @GetMapping(value = {"test/db/user/{id}"}, produces = "text/html")
    public String runDbTestUser(@PathVariable String id, HttpServletRequest req) throws IOException {
        checkHost(req);
        Optional<User> opt = _userService.findById(id);
        return genResponse(opt, "User", id.toString(), req);
    }


    @GetMapping(value = {"test/db/user/email/{address}"}, produces = "text/html")
    public String runDbTestUserEmail(@PathVariable String address, HttpServletRequest req) throws IOException {
        checkHost(req);
        List<User> users = _userService.findByEmail(address);
        return this.generateJsonView(req, this.serialize(users));
    }


    @GetMapping(value = {"test/db/activitylevel/{id}"}, produces = "text/html")
    public String runDbTestActivityLevel(@PathVariable long id, HttpServletRequest req) throws IOException {
        checkHost(req);

        Optional<ActivityLevel> opt = _activityLevelService.findById(id);
        return genResponse(opt, "ActivityLevel", String.valueOf(id), req);
    }

    @GetMapping(value = {"test/db/client/{id}"}, produces = "text/html")
    public String runDbTestClient(@PathVariable int id, HttpServletRequest req) throws IOException {
        checkHost(req);
        Optional<Client> opt = _clientService.findById(id);
        return genResponse(opt, "Client", String.valueOf(id), req);
    }

    @GetMapping(value = {"test/db/simpletrack/{id}"}, produces = "text/html")
    public String runDbTestTrack(@PathVariable UUID id, HttpServletRequest req) throws IOException {
        checkHost(req);
        Optional<SimpleTrack> opt = _trackService.findById(id.toString());
        return genResponse(opt, "SimpleTrack", id.toString(), req);
    }


    @GetMapping(value = {"test/db/datacenterconfig/template/{top}"}, produces = "text/html")
    public String testDcTemplate(@PathVariable int top, HttpServletRequest req) throws IOException {
        checkHost(req);
        Collection<Map<String, Object>> rows = _jdbcTemplate.queryForList(String.format("select top %s * from DataCenterConfig order by LastChecked desc;", top));
        return this.generateJsonView(req, this.serialize(rows));
    }

    @GetMapping(value = {"test/db/user/template/{top}"}, produces = "text/html")
    public String testUserTemplate(@PathVariable int top, HttpServletRequest req) throws IOException {
        checkHost(req);
        Collection<Map<String, Object>> rows = _jdbcTemplate.queryForList(String.format("select top %s * from Users order by ModifiedDateTime desc;", top));
        return this.generateJsonView(req, this.serialize(rows));
    }

    @GetMapping(value = {"test/db/activitylevel/template/{top}"}, produces = "text/html")
    public String testActivityLevelTemplate(@PathVariable int top, HttpServletRequest req) throws IOException, JsonProcessingException {
        checkHost(req);
        Collection<Map<String, Object>> rows = _jdbcTemplate.queryForList(String.format("select top %s * from ActivityLevel order by TrackDatetime desc;", top));
        return this.generateJsonView(req, this.serialize(rows));
    }

    @GetMapping(value = {"test/db/simpletrack/template/{top}"}, produces = "text/html")
    public String testSimpleTrackTemplate(@PathVariable int top, HttpServletRequest req) throws IOException {
        checkHost(req);
        Collection<Map<String, Object>> rows = _jdbcTemplate.queryForList(String.format("select top %s * from SimpleTrack order by TrackDatetime desc;", top));
        return this.generateJsonView(req, this.serialize(rows));
    }

    @GetMapping(value = {"test/db/client/template/{top}"}, produces = "text/html")
    public String testClientTemplate(@PathVariable int top, HttpServletRequest req) throws IOException {
        checkHost(req);
        Collection<Map<String, Object>> rows = _jdbcTemplate.queryForList(String.format("select top %s * from Client order by Lname asc;;", top));
        return this.generateJsonView(req, this.serialize(rows));
    }

    @GetMapping(value = {"test/oauth2"}, produces = "text/html")
    public String testLogins(HttpServletRequest req) throws IOException {
        checkHost(req);

        List<DataCenterConfig> subs = _dcService.findAllFitbitActive();
        LocalDateTime dtNow = LocalDateTime.now();
        Map<String, String> credsMap = new HashMap<>();
        Map<String, Object> result = new HashMap<>();

        for(DataCenterConfig dc : subs) {
            try {
                OAuthCredentials creds = dc.getOAuthCredentials();
                boolean valid = !creds.isExpired();
                credsMap.put(dc.getFkUserGuid(), String.format("UserId: %s, OAuth2 access token still valid: %s, expirate date: %s", dc.getFkUserGuid(), valid, creds.getExpiresAt()));
            }
            catch (Exception ex) {
                String msg = String.format("Error processing user: %s, error: %s", dc.getFkUserGuid(), ex.getMessage());
                _logger.error(StringUtils.formatError(msg, ex));
                credsMap.put(dc.getFkUserGuid(), msg);
            }
        }
        result.put("result", credsMap);
        return this.generateJsonView(req, this.serialize(result));
    }

    @GetMapping(value = {"/process/{id}/{dtStartStr}/{dtEndStr}"}, produces = "text/html")
    public String processOne(@PathVariable UUID id, @PathVariable String dtStartStr, @PathVariable String dtEndStr,
                             HttpServletRequest req) throws IOException {
        checkHost(req);

        Map<String, String> result = new HashMap<>();
        Optional<DataCenterConfig> optDc = _dcService.findById(id.toString(), SimpleTrack.SourceSystem.FITBIT.sourceSystem);
        if (!optDc.isPresent()) {
            return genResponse(optDc, "ActivityLevel", id.toString(), req);
        }
        DataCenterConfig dc = optDc.get();
        try {
            LocalDateTime dtStart = LocalDateTime.parse(StringUtils.fixDateTimeStr(dtStartStr));
            LocalDateTime dtEnd = LocalDateTime.parse(StringUtils.fixDateTimeStr(dtEndStr));

            _fitbitService.processAll(dc, dtStart, dtEnd);
            String msg = String.format("Successfully processed API data for %s", dc.getFkUserGuid());
            _logger.info(msg);
            result.put("result", msg);
        }
        catch (Exception ex) {
            String msg = String.format("An error occurred while processing API data for %s", dc.getFkUserGuid());
            _logger.error(msg, ex);
            result.put("result", msg);
            result.put("error", ex.getMessage());
        }
        return this.generateJsonView(req, this.serialize(result));
    }

    @GetMapping(value = {"/steps/{userId}/{reauth}"}, produces = "text/html")
    public String getSteps(@PathVariable String userId, String reauth, HttpServletRequest req) throws IOException {
        try {
            boolean doReauth = Boolean.parseBoolean(reauth);
            StepsData data = _fitbitService.getSteps(userId.toString(), doReauth);
            return this.generateJsonView(req, this.serialize(data));
        }
        catch (IOException | InterruptedException | ExecutionException ex) {
            String msg = String.format("{ \"error\" : \"Error processing user %s, error: %s\"}", userId, ex.getMessage());
            return this.generateJsonView(req, msg);
            //throw HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, msg, null, msg.getBytes(), null);
        }
        catch (IllegalArgumentException ex) {
            String msg = String.format("{ \"error\" : \"User %s does not exist in DataCenterConfig table.\"}", userId);
            return this.generateJsonView(req, msg);
        }
        catch (Exception ex) {
            String msg = String.format("An error occurred while processing API data for %s: %s", userId, ex.getMessage());
            _logger.error(msg, ex);
            return this.generateJsonView(req, this.genMessage("error", msg));
        }
    }

    @GetMapping(value = {"/auth2data"}, produces = "text/html")
    public String doItAll(HttpServletRequest req) throws IOException {
        Map<String, String> results = new TreeMap<>();

        final String clientId = _config.getFitbitClientId();
        final String clientSecret = _config.getFitbitClientSecret();

        final OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope(_config.getFitbitScope())
                //your callback URL to store and handle the authorization code sent by Fitbit
                .callback(_config.getFitbitCallbackUrl())
                .build(FitbitApi20.instance());

        // Obtain the Authorization URL
        final String authUrl = service.getAuthorizationUrl();

        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        try {
            HtmlPage page = client.getPage(authUrl);
            results.put(Instant.now().toEpochMilli() + ": " + "Loaded Auth URL", "success - " + authUrl);

            HtmlInput inputEmail = page.getFirstByXPath("//input[@tabindex='23']");
            HtmlInput inputPwd = page.getFirstByXPath("//input[@tabindex='24']");

            inputEmail.setValueAttribute(_config.getFitbitTestUser());
            inputPwd.setValueAttribute(_config.getFitbitTestPassword());

            //get the enclosing form
            HtmlForm loginForm = inputPwd.getEnclosingForm() ;

            //submit the form
            client.getOptions().setJavaScriptEnabled(true);
            page = client.getPage(loginForm.getWebRequest(null));

            results.put(Instant.now().toEpochMilli() + ": " + "Submitted login form", "success");

            if (!page.getBaseURL().toString().contains(_config.getFitbitCallbackUrl())) {

                HtmlCheckBoxInput inputSelectAll = page.getFirstByXPath("//input[@id='selectAllScope']");
                inputSelectAll.setChecked(true);

                //get the enclosing form
                HtmlForm allowForm = inputSelectAll.getEnclosingForm();
                HtmlButton allowBtn = page.getFirstByXPath("//button[@id='allow-button']");

                page = client.getPage(allowForm.getWebRequest(allowBtn));
                results.put(Instant.now().toEpochMilli() + ": " + "Fetched Reauth Page", "success");
            }

            String qs = page.getBaseURL().getQuery();
            String code = qs.split("=")[1];

            int ts = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));

            User user;
            List<User> users = _userService.findByEmail(_config.getFitbitTestUser());
            if (users.size() > 0) {
                user = users.get(0);
                results.put(Instant.now().toEpochMilli() + ": " + "Found user", "success - " + _config.getFitbitTestUser());
            }
            else {
                user = new User(ts, 1, _config.getFitbitTestUser(), "", "", "98801", UUID.randomUUID().toString());
                user = _userService.createUser(user);
                results.put(Instant.now().toEpochMilli() + ": " + "Created new user", "success - " + _config.getFitbitTestUser());
            }

            OAuthCredentials creds = _fitbitService.getAuth(code);
            results.put(Instant.now().toEpochMilli() + ": " + "Got OAuth token", "success");

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
                results.put(Instant.now().toEpochMilli() + ": " + "Found DataCenterConfig", "success - " + dc.getFkUserGuid());
                //creds = dc.getOAuthCredentials();
            }
            else {
                dc = new DataCenterConfig(user.getUserGuid(), SimpleTrack.SourceSystem.FITBIT.sourceSystem, lastChecked,
                        0, Active.status, "OK", 0, json, modified);
                dc = _dcService.createDataCenterConfig(dc);
                results.put(Instant.now().toEpochMilli() + ": " + "Created new DataCenterConfig", "success - " + dc.getFkUserGuid());
            }

            long startTs = Instant.now().toEpochMilli();

            results.put(Instant.now().toEpochMilli() + ": " + "Starting FitBit API requests...", "success");

            LocalDateTime dtEnd = LocalDateTime.now();
            LocalDateTime dtStart = dtEnd.minusDays(_config.getFitbitQueryWindow());
            _fitbitService.processAll(dc, dtStart, dtEnd);

            long endTs = Instant.now().toEpochMilli();
            results.put(Instant.now().toEpochMilli() + ": " + "Finished FitBit API requests", String.format("success, elapsed time: %s milliseconds", (endTs - startTs)) );
        }
        catch(Exception ex){
            results.put(Instant.now().toEpochMilli() + ": " + "Error", ex.getMessage());
            results.put(Instant.now().toEpochMilli() + ": " + "Stack", StringUtils.getStackTrace(ex));
        }

        return this.generateJsonView(req, this.serialize(results));
    }
}
