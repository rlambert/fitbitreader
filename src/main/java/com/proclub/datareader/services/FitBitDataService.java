package com.proclub.datareader.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.apis.fitbit.FitBitOAuth2AccessToken;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.*;
import com.proclub.datareader.model.FitBitApiData;
import com.proclub.datareader.model.activitylevel.ActivityLevelData;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.model.sleep.Sleep;
import com.proclub.datareader.model.sleep.SleepData;
import com.proclub.datareader.model.steps.Steps;
import com.proclub.datareader.model.steps.StepsData;
import com.proclub.datareader.model.weight.Weight;
import com.proclub.datareader.model.weight.WeightData;
import com.proclub.datareader.utils.StringUtils;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@SuppressWarnings("unused")
public class FitBitDataService {

    // ----------------------------- static class data

    @Data
    private static class DynaUrl {
        private String url;
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        DynaUrl(String url, LocalDateTime dtStart, LocalDateTime dtEnd) {
            this.url = url;
            this.startDate = dtStart.truncatedTo(ChronoUnit.DAYS);
            this.endDate = dtEnd.truncatedTo(ChronoUnit.DAYS);
        }
    }

    private static Logger _logger = LoggerFactory.getLogger(FitBitDataService.class);


    // ----------------------------- instance data

    //get current TimeZone using getTimeZone method of Calendar class
    // TODO: Update LocalDateTime instances with server's zone
    private static TimeZone _timeZone = Calendar.getInstance().getTimeZone();

    private AppConfig _config;                          // application config
    private UserService _userService;                   // our service to fetch user data
    private DataCenterConfigService _dcService;         // get DataCenterConfig table
    private ActivityLevelService _activityLevelService; // get/put ActivityLevel table
    private SimpleTrackService _trackService;           // get/put SimpleTrack table data
    private ClientService _clientService;               // gets Client table data
    private EmailService _emailService;                 // centralized email code
    private AuditLogService _auditService;              // audit logs
    private final OAuth20Service _oauthService;         // this is a scribejava library instance

    private OkHttpClient _client = new OkHttpClient();  // for accessing web API
    private ObjectMapper _mapper = new ObjectMapper();  // for deserializing JSON


    // ----------------------------- constructor

    /**
     * ctor
     *
     * @param config - AppConfig
     * @param userService - UserService
     * @param dcService - DataCenterConfigService
     * @param activityLevelService - ActivityLevelService
     * @param trackService - SimpleTrackService
     */
    @Autowired
    public FitBitDataService(AppConfig config, UserService userService, DataCenterConfigService dcService,
                             ActivityLevelService activityLevelService, SimpleTrackService trackService,
                             ClientService clientService, EmailService emailService, AuditLogService auditService) {
        _config = config;
        _userService = userService;
        _dcService = dcService;
        _activityLevelService = activityLevelService;
        _trackService = trackService;
        _clientService = clientService;
        _emailService = emailService;
        _auditService = auditService;

        // we need the config instance to build the library object,
        // so we instantiate the service object here
        _oauthService = new ServiceBuilder(_config.getFitbitClientId())
                .apiSecret(_config.getFitbitClientSecret())
                .scope(_config.getFitbitScope())
                .callback(_config.getFitbitCallbackUrl())
                .build(FitbitApi20.instance());
    }

    // ----------------------------- methods

    private void auditEvent(String fkUserId, AuditLog.Activity evt, String details) {
        // String fkUserGuid, LocalDateTime dateTime, Activity activity, String details
        AuditLog log = new AuditLog(fkUserId, LocalDateTime.now(), evt, details);
        _auditService.createOrUpdate(log);
    }

    /**
     * private helper method to extract JSON string from responses
     * @param response - Response
     * @return String - JSON result
     * @throws IOException - on I/O error
     */
    private String processResponseBody(Response response) throws IOException {
        String json = "";
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected response code: " + response);
        }
        else {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                json = responseBody.string();
            }
            response.close();
        }
        return json;
    }

    /**
     * Once a user has clicked on the authorization URL and allowed our app,
     * we get called with their verify code. This method uses that code to
     * request an OAuth2 token, which is then returned in a JWT. That token
     * is converted to ProClub's tweaked OAuthCredentials object and returned.
     * @param code - String
     * @return OAuthCredentials - includes OAuth access and refresh tokens
     * @throws InterruptedException - not normal
     * @throws ExecutionException - also not likely
     * @throws IOException - if FitBit.com API is down
     * @throws IllegalArgumentException - if values sent to API are incorrect
     */
    public OAuthCredentials getAuth(String code) throws InterruptedException, ExecutionException, IOException, IllegalArgumentException {
        // trade the Request Token and Verifier Code for the Access Token
        OAuth2AccessToken oauth2AccessToken = _oauthService.getAccessToken(code);
        if (!(oauth2AccessToken instanceof FitBitOAuth2AccessToken)) {
            String errmsg = String.format("oauth2AccessToken is not instance of FitBitOAuth2AccessToken: code=%s, token: %s", code, oauth2AccessToken);
            throw new IllegalArgumentException(errmsg);
        }

        return new OAuthCredentials((FitBitOAuth2AccessToken) oauth2AccessToken);
    }

    /**
     * Uses the refresh token to request a new OAuth2 token
     * @param refreshToken - String
     * @return OAuthCredentials
     * @throws InterruptedException - not normal
     * @throws ExecutionException - also not likely
     * @throws IOException - if FitBit.com API is down
     */
    private OAuthCredentials refreshToken(String refreshToken) throws IOException, ExecutionException, InterruptedException {
        OAuth2AccessToken token = _oauthService.refreshAccessToken(refreshToken);
        // the original token already had to have been a FitBit token or we
        // wouldn't be here
        return new OAuthCredentials((FitBitOAuth2AccessToken) token);
    }

    /**
     * convenience overload
     * @param oldCreds - OAuthCredentials
     * @return OAuthCredentials
     * @throws InterruptedException - not normal
     * @throws ExecutionException - also not likely
     * @throws IOException - if FitBit.com API is down
     */
    private OAuthCredentials refreshToken(OAuthCredentials oldCreds) throws IOException, ExecutionException, InterruptedException {
        return refreshToken(oldCreds.getRefreshToken());
    }

    /**
     * helper method to build an API request
     * @param dataClass - FitBitData implementation
     * @param apiUrl - FitBit API URL
     * @param oauth - OAuth credentials
     * @param dt - LocalDateTime
     * @return FitBitApiData
     */
    private FitBitApiData makeApiRequest(Class dataClass, String apiUrl, OAuthCredentials oauth, LocalDateTime dt)
            throws IOException, ExecutionException, InterruptedException {

        Request req = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + oauth.getAccessToken())
                .header("Accept-Language", "en_US")
                .build();

        FitBitApiData objData = null;
        try (Response response = _client.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                    // reauthorize (will throw an exception if it fails)
                    oauth = refreshToken(oauth);
                    return makeApiRequest(dataClass, apiUrl, oauth, dt);
                }
            }
            else {
                String json = processResponseBody(response);
                _logger.info(String.format("json: %s", json));
                objData = (FitBitApiData) _mapper.readValue(json, dataClass);
            }
        }
        catch (JsonParseException ex) {
            _logger.error(StringUtils.formatError(String.format("JSON parse error processing %s", apiUrl), ex));
        }
        catch (IOException | ExecutionException | InterruptedException ex) {
            _logger.error(StringUtils.formatError(String.format("Error processing %s", apiUrl), ex));
            throw ex;
        }
        return objData;
    }

    /**
     * helper method to find an existing ActivityLevel
     * @param fitbitData - ActivityLevel instance
     * @param dbActivityLevels - ActvityLeved list from database
     * @return Optional<ActivityLevel>
     */
    private Optional<ActivityLevel> findActivityLevelMatch(ActivityLevel fitbitData, List<ActivityLevel> dbActivityLevels) {
        for(ActivityLevel row : dbActivityLevels) {
            if (row.getTrackDateTime().isEqual(fitbitData.getTrackDateTime())) {
                return Optional.of(row);
            }
        }
        return Optional.empty();
    }

    /**
     * getActivityLevels returns ActivityLevel data for a single day from the
     * FitBit API
     * @param dt - Instant
     * @return - SleepData
     * @throws IOException on API error
     */
    public List<ActivityLevelData> getActivityLevels(DataCenterConfig dc, LocalDateTime dt)
                    throws IOException, ExecutionException, InterruptedException {
        LocalDateTime dtMidnight = LocalDateTime.of(dt.getYear(), dt.getMonth(), dt.getDayOfMonth(), 0, 0, 0, 0);

        // go back in time a configurable number of days
        LocalDateTime dtStart = dtMidnight.minusDays(_config.getFitbitActivityLevelWindowDays());
        String baseUrl = _config.getFitbitActivityUrl();

        // now iterate through each day in window
        LocalDateTime dtloop = dtStart;
        LocalDateTime dtEnd = dt.plusDays(1).truncatedTo(ChronoUnit.DAYS);

        // these are in TrackDateOrder
        List<ActivityLevel> dbActivityLevels = _activityLevelService.findByUserAndTrackDateWindow(dc.getFkUserGuid(), dtStart, dtEnd);

        List<ActivityLevelData> apiResults = new ArrayList<>();

        String url;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (dtloop.isBefore(dtEnd)) {
            url = baseUrl.replace("${date}", dtloop.format(fmt));
            // ask FitBit API for this day
            ActivityLevelData data = (ActivityLevelData) makeApiRequest(ActivityLevelData.class, url, dc.getOAuthCredentials(), dtloop);
            if (data != null) {
                // convert to our DTO
                ActivityLevel activityLevel = new ActivityLevel(dc.getFkUserGuid().toString(), dtloop, data);
                Optional<ActivityLevel> optMatch = findActivityLevelMatch(activityLevel, dbActivityLevels);
                if (optMatch.isPresent()) {
                    ActivityLevel dbLevel = optMatch.get();
                    activityLevel.setActivityLevelId(dbLevel.getActivityLevelId());
                }

                // this is an insert (if no match already in DB) or update
                activityLevel = _activityLevelService.saveActivityLevel(activityLevel);
                apiResults.add(data);
                _logger.info(StringUtils.formatMessage(String.format("ActivityLevelData saved for %s - %s", dtloop.toString(), dc.getFkUserGuid())));
                dtloop = dtloop.plusDays(1);
            }
            else {
                _logger.info(StringUtils.formatMessage(String.format("No ActivityLevelData available for %s - %s", dtloop.toString(), dc.getFkUserGuid())));
            }
        }
        return apiResults;
    }


    /**
     * helper method to centralize URL creation for sleep,
     * steps, and weight.
     * @param baseUrl - LocalDateTime
     * @param dt - LocalDateTime
     * @param windowDays - int
     * @return DynaUrl
     */
    private DynaUrl prepUrl(String baseUrl, LocalDateTime dt, int windowDays) {
        LocalDateTime dtStart = dt.minusDays(windowDays);
        LocalDateTime dtEnd   = dt.plusDays(1);

        // get all API data for our date-range
        String dtStartStr = dtStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dtEndStr = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));         // original date is end of range

        // https://api.fitbit.com/1.2/user/-/xxxx/date/${startDate}/${endDate}.json
        String resultUrl = baseUrl.replace("${startDate}", dtStartStr).replace("${endDate}", dtEndStr);
        return new DynaUrl(resultUrl, dtStart, dtEnd);
    }

    /**
     * helper to find a given Weight instance in a list of
     * DB results for Weight
     * @param weight - Weight
     * @param dbResults - List&lt;SimpleTrack&gt;
     * @return Optional&lt;SimpleTrack&gt;
     */
    private Optional<SimpleTrack> findWeightMatch(Weight weight, List<SimpleTrack> dbResults) {
        String dtStr = weight.getDate() + "T" + weight.getTime() + ".000";
        LocalDateTime dt = LocalDateTime.parse(dtStr);
        ZoneOffset zos = ZoneOffset.ofHours(0);

        for (SimpleTrack item : dbResults) {
            if (item.getTrackDateTime() == dt.toEpochSecond(zos)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * getWeight returns weight data for a single day from the
     * FitBit API
     * @param dt - Instant
     * @return - SleepData
     * @throws InterruptedException - not normal
     * @throws ExecutionException - also not likely
     * @throws IOException - if FitBit.com API is down
     */
    public WeightData getWeight(DataCenterConfig dc, LocalDateTime dt) throws IOException, ExecutionException, InterruptedException {
        DynaUrl urlInfo = prepUrl(_config.getFitbitWeightUrl(), dt, _config.getFitbitWeightWindowDays());
        WeightData apiResults = (WeightData) makeApiRequest(WeightData.class, urlInfo.getUrl(), dc.getOAuthCredentials(), dt);

        List<SimpleTrack> dbResults = _trackService.findByUserTrackDateRange(dc.getFkUserGuid(), urlInfo.getStartDate(), urlInfo.getEndDate(), SimpleTrack.Entity.WEIGHT);

        for(Weight wt : apiResults.getWeight()) {
            Optional<SimpleTrack> result = findWeightMatch(wt, dbResults);
            SimpleTrack newTracker = new SimpleTrack(wt, dc.getFkUserGuid());
            if (result.isPresent()) {
                SimpleTrack dbWt = result.get();
                newTracker.setSimpleTrackGuid(dbWt.getSimpleTrackGuid());
            }
            // this will update an existing record if there is an ID
            _trackService.createSimpleTrack(newTracker);
        }
        return apiResults;
    }

    /**
     * helper to find a given Sleep instance in a list of
     * DB results for Sleep data
     * @param dbResults - List&lt;SimpleTrack&gt;
     * @param sleep - Sleep
     * @return Optional&lt;SimpleTrack&gt;
     */
    private Optional<SimpleTrack> findSleepMatch(Sleep sleep, List<SimpleTrack> dbResults) {
        String dtStr = sleep.getDateOfSleep();
        if (!dtStr.contains(":")) {
            dtStr += "T00:00:00.000";
        }
        LocalDateTime dt = LocalDateTime.parse(dtStr);
        ZoneOffset zos = ZoneOffset.ofHours(0);

        for (SimpleTrack item : dbResults) {
            if (item.getTrackDateTime() == dt.toEpochSecond(zos)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * getSleep returns sleep data for a single day from the
     * FitBit API
     * @param dt - Instant
     * @return - SleepData
     * @throws IOException on API error
     */
    public SleepData getSleep(DataCenterConfig dc, LocalDateTime dt) throws IOException, ExecutionException, InterruptedException {

        DynaUrl urlInfo = prepUrl(_config.getFitbitSleepUrl(), dt, _config.getFitbitSleepWindowDays());
        SleepData sleepData = (SleepData) makeApiRequest(SleepData.class, urlInfo.getUrl(), dc.getOAuthCredentials(), dt);

        // get all database rows that overlap; go back in time a configurable number of days
        List<SimpleTrack> dbResults = _trackService.findByUserTrackDateRange(dc.getFkUserGuid(), urlInfo.getStartDate(), urlInfo.getEndDate(), SimpleTrack.Entity.SLEEP);

        for(Sleep sleep : sleepData.getSleep()) {
            Optional<SimpleTrack> result = findSleepMatch(sleep, dbResults);
            SimpleTrack newTracker = new SimpleTrack(sleep, dc.getFkUserGuid());
            if (result.isPresent()) {
                SimpleTrack dbWt = result.get();
                newTracker.setSimpleTrackGuid(dbWt.getSimpleTrackGuid());
            }
            // this will update an existing record if there is an ID
            // or save as new if not
            _trackService.createSimpleTrack(newTracker);
        }
        return sleepData;
    }

    /**
     * helper to find a given Sleep instance in a list of
     * DB results for Sleep data
     * @param dbResults - List&lt;SimpleTrack&gt;
     * @param steps - Steps
     * @return Optional&lt;SimpleTrack&gt;
     */
    private Optional<SimpleTrack> findStepsMatch(Steps steps, List<SimpleTrack> dbResults) {
        String dtStr = steps.getDateTime();
        if (!dtStr.contains(":")) {
            dtStr += "T00:00:00.000";
        }
        LocalDateTime dt = LocalDateTime.parse(dtStr);
        ZoneOffset zos = ZoneOffset.ofHours(0);

        for (SimpleTrack item : dbResults) {
            if (item.getTrackDateTime() == dt.toEpochSecond(zos)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * helper method to refresh credentials
     * @param dc - DataCenterConfig
     * @param dtNow - LocalDateTime
     * @param suppressNotifications - boolean
     * @return OAuthCredentials
     */
    private Optional<OAuthCredentials> checkRefresh(DataCenterConfig dc, LocalDateTime dtNow, boolean suppressNotifications) {
        OAuthCredentials creds;
        creds = dc.getOAuthCredentials();

        if (creds == null) {
            // can't create them... fatal error for this user
            String msg = String.format("No OAuth credentials stored in database for user ID: %s", dc.getFkUserGuid());
            _logger.error(StringUtils.formatMessage(msg));
            updateDataCenterConfigError(dc, dtNow);
            auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError,
                    String.format("DataCentConfig has no credentials for '%s'", dc.getFkUserGuid()));
            return Optional.empty();
        }
        if (creds.isExpired()) {
            // need to refresh
            try {
                updateDataCenterConfigDuringRefresh(dc, dtNow);
                // if we can refresh token...
                creds = refreshToken(creds);
                if (creds.getExpirationDt().isAfter(dtNow)) {
                    // ...then we need to update the database as well
                    dc.setCredentials(creds.toJson());
                    updateDataCenterConfigSuccess(dc, dtNow);
                    auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshedCredentials,
                            String.format("Refreshed OAuth2 credentials successfully for FitBit user '%s'", creds.getAccessUserId()));
                }
                else {
                    updateDataCenterConfigError(dc, dtNow);
                    auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError, String.format("Unsuccessful token refresh, credentials expired."));
                }
            }
            catch(IOException | ExecutionException | InterruptedException ex) {
                String msg = String.format("Could not refresh OAuth2 token for user '%s'", dc.getFkUserGuid());
                _logger.error(msg, ex);
                // send notification email if enabled
                if (!suppressNotifications) {
                    notifyUser(dc);
                }
                updateDataCenterConfigError(dc, dtNow);
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError, String.format("Unsuccessful token refresh: %s", ex.getMessage()));
                return Optional.empty();
            }
        }  // end if creds expired
        return Optional.of(creds);
    }


    /**
     * helper method to look up DataCenterConfig row given a userId
     * @param userId - String (UUID)
     * @return DataCenterConfig
     */
    private DataCenterConfig getDataCenterConfig(String userId) {
        Optional<DataCenterConfig> optDc = _dcService.findById(userId, SimpleTrack.SourceSystem.FITBIT.sourceSystem);
        if (!optDc.isPresent()) {
            throw new IllegalArgumentException(String.format("UserId '%s' is not in DataCenterConfig table", userId));
        }
        return optDc.get();
    }

    /**
     * self-contained method to return Steps data
     * @param fkUserId - String
     * @param suppressReauth - boolean
     * @return StepsData
     * @throws IOException on API error
     */
    public StepsData getSteps(String fkUserId, boolean suppressReauth) throws IOException, ExecutionException, InterruptedException {

        DataCenterConfig dc = getDataCenterConfig(fkUserId);
        LocalDateTime dtNow = LocalDateTime.now();
        OAuthCredentials creds = dc.getOAuthCredentials();
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, dtNow, suppressReauth);
        if (optCreds.isPresent()) {
            return getSteps(dc, dtNow);
        }
        throw new IOException("No OAuthCredentials available for " + fkUserId);
    }


    /**
     * self-contained method to return Sleep data
     * @param fkUserId - String
     * @param suppressReauth - boolean
     * @return SleepData
     * @throws IOException on API error
     */
    public SleepData getSleep(String fkUserId, boolean suppressReauth) throws IOException, ExecutionException, InterruptedException {
        DataCenterConfig dc = getDataCenterConfig(fkUserId);
        LocalDateTime dtNow = LocalDateTime.now();
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, dtNow, suppressReauth);
        if (optCreds.isPresent()) {
            return getSleep(dc, dtNow);
        }
        throw new IOException("No OAuthCredentials available for " + fkUserId);
    }

    /**
     * self-contained method to return Weight data
     * @param fkUserId - String
     * @param suppressReauth - boolean
     * @return WeightData
     * @throws IOException on API error
     */
    public WeightData getWeight(String fkUserId, boolean suppressReauth) throws IOException, ExecutionException, InterruptedException {
        DataCenterConfig dc = getDataCenterConfig(fkUserId);
        LocalDateTime dtNow = LocalDateTime.now();
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, dtNow, suppressReauth);
        if (optCreds.isPresent()) {
            return getWeight(dc, dtNow);
        }
        throw new IOException("No OAuthCredentials available for " + fkUserId);
    }

    /**
     * self-contained method to return ActivityLevel data
     * @param fkUserId - String
     * @param suppressReauth - boolean
     * @return List<ActivityLevelData>
     * @throws IOException on API error
     */
    public List<ActivityLevelData> getActivityLevel(String fkUserId, boolean suppressReauth) throws IOException, ExecutionException, InterruptedException {
        DataCenterConfig dc = getDataCenterConfig(fkUserId);
        LocalDateTime dtNow = LocalDateTime.now();
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, dtNow, suppressReauth);
        if (optCreds.isPresent()) {
            return getActivityLevels(dc, dtNow);
        }
        throw new IOException("No OAuthCredentials available for " + fkUserId);
    }


    /**
     * getSteps returns FitBet steps data for a single day
     * @param dc - DataCenterConfig
     * @param dt - LocalDateTime
     * @return - StepsData
     * @throws IOException on API error
     */
    public StepsData getSteps(DataCenterConfig dc, LocalDateTime dt)
             throws IOException, ExecutionException, InterruptedException {

        DynaUrl urlInfo = prepUrl(_config.getFitbitStepsUrl(), dt, _config.getFitbitStepsWindowDays());
        StepsData apiResults = (StepsData) makeApiRequest(StepsData.class, urlInfo.getUrl(), dc.getOAuthCredentials(), dt);

        List<SimpleTrack> dbResults = _trackService.findByUserTrackDateRange(dc.getFkUserGuid(), urlInfo.getStartDate(), urlInfo.getEndDate(), SimpleTrack.Entity.STEPS);

        for(Steps steps : apiResults.getSteps()) {
            Optional<SimpleTrack> result = findStepsMatch(steps, dbResults);
            SimpleTrack newTracker = new SimpleTrack(steps, dc.getFkUserGuid());
            if (result.isPresent()) {
                SimpleTrack dbWt = result.get();
                newTracker.setSimpleTrackGuid(dbWt.getSimpleTrackGuid());
            }
            // this will update an existing record if there is an ID
            _trackService.createSimpleTrack(newTracker);
        }

        return apiResults;

        /*
        final StepsData stepsData;
        _client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException ex) {
                String msg = String.format("FitBit Steps API failure for userId: %s", oauth.getAccessUserId());
                _logger.error(StringUtils.formatError(msg, ex));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                            // get new token with refresh_token
                        }
                        else {
                            throw new IOException("Unexpected response code: " + response);
                        }
                    }
                    String json = responseBody.toString();
                    stepsData = _mapper.readValue(json, StepsData.class);
                    SimpleTrack tracker = new SimpleTrack(stepsData);
                    _trackService.createSimpleTrack(tracker);
                }
            }
        });
        */
    }

    /**
     * helper method to determine if we should send the
     * reauthorization email to a customer
     * @param dc - DataCenterConfig
     * @return boolean, true if we're in time to send
     */
    private boolean isInAuthWindow(DataCenterConfig dc) {
        LocalDateTime dt = dc.getOAuthCredentials().getExpirationDt();
        LocalDateTime dtLater = dt.plusDays(_config.getFitbitReauthWindowDays());
        // if the expiration time plus the reauth window is AFTER
        // right now, then we DO want to ask them to reauth
        return LocalDateTime.now().isAfter(dtLater);
    }

    /**
     * helper method to notify users via email if they need to reauthorize
     * the application
     * @param dc - DataCenterConfig
     */
    private void notifyUser(DataCenterConfig dc) {
        // handle an auth error: is authorization email enabled?
        if (_config.isFitbitSendAuthEmail()) {

            // are we within the time window to even send the email?
            if (isInAuthWindow(dc)) {

                // yes, so see if we can find the user's email

                _logger.info(StringUtils.formatMessage(String.format("Attempting auth email for user '%s'", dc.getFkUserGuid())));
                Optional<User> opt = _userService.findById(dc.getFkUserGuid());
                if (opt.isPresent()) {
                    User user = opt.get();
                    if (!StringUtils.isNullOrEmpty(user.getEmail())) {
                        String fname = "";
                        List<Client> clientList = _clientService.findByEmail(user.getEmail());
                        if (clientList.size() == 0) {
                            _logger.error("No Client row with matching User.email of '%s'", user.getEmail());
                        } else {
                            Client client = clientList.get(0);
                            fname = client.getFname();
                        }

                        // notify user with email (emailService logs send errors)
                        _emailService.sendTemplatedEmail(user.getEmail(), fname);
                        _logger.info(StringUtils.formatMessage(String.format("Sent auth email to '%s'for user '%s'", user.getEmail(), dc.getFkUserGuid())));
                    } else {
                        // however unlikely, we should at least log not having an email addy
                        _logger.error(String.format("No email address in User table for userId: %s", dc.getFkUserGuid()));
                    }
                } else {
                    _logger.error(String.format("User '%s' not found in User table.", dc.getFkUserGuid()));
                }
            }
        }
    }

    /**
     * helper method to update DataCenterConfig row
     * @param dc - DataCenterConfig
     * @param dt - LocalDateTime
     */
    private void updateDataCenterConfigError(DataCenterConfig dc, LocalDateTime dt) {
        dc.setStatus(DataCenterConfig.PartnerStatus.RefreshErr.status);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        dc.setStatusText(String.format("Refresh Error for day: %s", dt.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        _dcService.updateDataCenterConfig(dc);
    }

    /**
     * mark a DataCenterConfig as a refresh in progress
     * @param dc - DataCenterconfig
     * @param dt - LocalDateTime
     */
    private void updateDataCenterConfigDuringRefresh(DataCenterConfig dc, LocalDateTime dt) {
        dc.setStatus(DataCenterConfig.PartnerStatus.AuthErr.status);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        dc.setStatusText("Refresh in progress");
        _dcService.updateDataCenterConfig(dc);
    }

    /**
     * helper method to update the DataCenterConfig row after a successful
     * authorization with the FitBit API
     * @param dc - DataCenterConfig
     * @param dt - LocalDateTime
     */
    private void updateDataCenterConfigSuccess(DataCenterConfig dc, LocalDateTime dt) {
        dc.setStatus(DataCenterConfig.PartnerStatus.Active.status);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, YYYY - hh:mm:ss a");
        dc.setStatusText(dt.format(fmt));
        dc.setLastChecked(dt);
        _dcService.updateDataCenterConfig(dc);
    }

    /**
     * gets all available FitBit data for a single user
     * @param userId - UUID
     * @throws IOException on internal API calls
     */
    public void processUser(UUID userId, boolean suppressNotifications) throws IOException {
        Optional<DataCenterConfig> optDc = _dcService.findById(userId.toString(), SimpleTrack.SourceSystem.FITBIT.sourceSystem);
        if (optDc.isPresent()) {
            DataCenterConfig dc = optDc.get();
            processAll(dc, LocalDateTime.now(), suppressNotifications);
        }
        else {
            throw new IllegalArgumentException(String.format("User %s not found in DataCenterConfig table", userId));
        }
    }

    public void processAll(DataCenterConfig dc, LocalDateTime dtNow) throws IOException {
        processAll(dc, dtNow, false);
    }

    /**
     * entry point for grabbing all available FitBit data for
     * a given user
     * @param dc - DataCenterConfig entry for use
     * @throws IOException on internal API call errors
     */
    public void processAll(DataCenterConfig dc, LocalDateTime dtNow, boolean suppressNotifications) throws IOException {

        // check if they're valid, and if not, see if we can refresh
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, dtNow, suppressNotifications);
        if (!optCreds.isPresent()) {
            // shouldn't ever happen, but just in case
            throw new IOException(String.format("UserId: %s, could not generate valid OAuthCredentials.", dc.getFkUserGuid()));
        }
        OAuthCredentials creds = optCreds.get();

        // if creds are not expired, our refresh was good, so
        // we can move forward with pulling data. If they ARE
        // expired, we fall out the bottom and exit.

        if (!creds.isExpired()) {
            // we've valid got credentials, so let's fetch us some ActivityLevel data
            try {
                getActivityLevels(dc, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("ActivityLevelData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.ActivityLevelRead, String.format("Successfully updated ActivityLevels"));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving ActivityLevelData for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating ActivityLevels: %s", ex.getMessage()));
            }

            // now fetch and save Sleep data
            try {
                getSleep(dc, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("SleepData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.SleepRead, String.format("Successfully updated Sleep data"));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Sleep data for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating Sleep: %s", ex.getMessage()));
            }

            // fetch and save Steps data
            try {
                getSteps(dc, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("StepsData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.StepsRead, String.format("Successfully updated Steps data"));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Steps data for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating Steps data: %s", ex.getMessage()));
            }


            // fetch and save Weight data
            try {
                getWeight(dc, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("WeightData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.WeightRead, String.format("Successfully updated Weight data"));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Weight data for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating Weight data: %s", ex.getMessage()));
            }
        } // end if not expired credentials
    }
}
