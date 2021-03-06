package com.proclub.datareader.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

import javax.mail.MessagingException;
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
    public static TimeZone _timeZone = Calendar.getInstance().getTimeZone();

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
    private ObjectMapper _mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // for deserializing JSON


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
    public OAuthCredentials refreshToken(String refreshToken) throws RuntimeException, IOException, ExecutionException, InterruptedException {
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
    private OAuthCredentials refreshToken(OAuthCredentials oldCreds) throws RuntimeException, IOException, ExecutionException, InterruptedException {
        return refreshToken(oldCreds.getRefreshToken());
    }

    /**
     * helper method to build an API request
     * @param dataClass - FitBitData implementation
     * @param apiUrl - FitBit API URL
     * @param oauth - OAuth credentials
     * @return FitBitApiData
     */
    private FitBitApiData makeApiRequest(Class dataClass, String apiUrl, OAuthCredentials oauth)
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
                    return makeApiRequest(dataClass, apiUrl, oauth);
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
     * @param dtStart - LocalDateTime
     * @param dtEnd - LocalDateTime
     * @return - SleepData
     * @throws IOException on API error
     */
    private List<ActivityLevelData> getActivityLevels(DataCenterConfig dc, LocalDateTime dtStart, LocalDateTime dtEnd)
                    throws IOException, ExecutionException, InterruptedException {
        //LocalDateTime dtMidnight = LocalDateTime.of(dt.getYear(), dt.getMonth(), dt.getDayOfMonth(), 0, 0, 0, 0);

        // go back in time a configurable number of days
        // LocalDateTime dtStart = dtMidnight.minusDays(_config.getFitbitActivityLevelWindowDays());
        String baseUrl = _config.getFitbitActivityUrl();

        // now iterate through each day in window
        LocalDateTime dtloop = dtStart;
        dtEnd = dtEnd.truncatedTo(ChronoUnit.DAYS);

        // these are in TrackDateOrder
        List<ActivityLevel> dbActivityLevels = _activityLevelService.findByUserAndTrackDateWindow(dc.getFkUserGuid(), dtStart, dtEnd);

        List<ActivityLevelData> apiResults = new ArrayList<>();

        String url;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (dtloop.isBefore(dtEnd)) {
            url = baseUrl.replace("${date}", dtloop.format(fmt));
            // ask FitBit API for this day
            ActivityLevelData data = (ActivityLevelData) makeApiRequest(ActivityLevelData.class, url, dc.getOAuthCredentials());
            if (data != null) {
                // convert to our DTO
                ActivityLevel activityLevel = new ActivityLevel(dc.getFkUserGuid(), dtloop, data);
                Optional<ActivityLevel> optMatch = findActivityLevelMatch(activityLevel, dbActivityLevels);
                if (optMatch.isPresent()) {
                    ActivityLevel dbLevel = optMatch.get();
                    activityLevel.setActivityLevelId(dbLevel.getActivityLevelId());
                }

                // this is an insert (if no match already in DB) or update
                _activityLevelService.saveActivityLevel(activityLevel);
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
     * @param baseUrl - String
     * @param dtStart - LocalDateTime
     * @param dtEnd - LocalDateTime
     * @return DynaUrl
     */
    private DynaUrl prepUrl(String baseUrl, LocalDateTime dtStart, LocalDateTime dtEnd) {
        //LocalDateTime dtStart = dt.minusDays(windowDays);
        dtEnd   = dtEnd.plusDays(1);

        // get all API data for our date-range
        String dtStartStr = dtStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dtEndStr = dtEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));         // original date is end of range

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
     * @param dc - DataCenterConfig
     * @param dtStart - LocalDateTime
     * @param dtEnd - LocalDateTime
     * @return - SleepData
     * @throws InterruptedException - not normal
     * @throws ExecutionException - also not likely
     * @throws IOException - if FitBit.com API is down
     */
    private WeightData getWeight(DataCenterConfig dc, LocalDateTime dtStart, LocalDateTime dtEnd) throws IOException, ExecutionException, InterruptedException {
        DynaUrl urlInfo = prepUrl(_config.getFitbitWeightUrl(), dtStart, dtEnd);
        WeightData apiResults = (WeightData) makeApiRequest(WeightData.class, urlInfo.getUrl(), dc.getOAuthCredentials());

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

        LocalDateTime dtDb;
        for (SimpleTrack item : dbResults) {
            dtDb = LocalDateTime.ofEpochSecond(item.getTrackDateTime(), 0, zos);
            if (dtDb.getDayOfYear() == dt.getDayOfYear()) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
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
        LocalDateTime dtEnd = LocalDateTime.now();
        LocalDateTime dtStart = dtEnd.minusDays(_config.getFitbitQueryWindow());
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, suppressReauth);
        if (optCreds.isPresent()) {
            return getSleep(dc, dtStart, dtEnd);
        }
        throw new IOException("No OAuthCredentials available for " + fkUserId);
    }

    /**
     * getSleep returns sleep data for a single day from the
     * FitBit API
     * @param dtStart - LocalDateTime
     * @param dtEnd - LocalDateTime
     * @return - SleepData
     * @throws IOException on API error
     */
    private SleepData getSleep(DataCenterConfig dc, LocalDateTime dtStart, LocalDateTime dtEnd)
                        throws IOException, ExecutionException, InterruptedException {

        DynaUrl urlInfo = prepUrl(_config.getFitbitSleepUrl(), dtStart, dtEnd);
        SleepData sleepData = (SleepData) makeApiRequest(SleepData.class, urlInfo.getUrl(), dc.getOAuthCredentials());

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

        LocalDateTime dtDb;
        for (SimpleTrack item : dbResults) {
            dtDb = LocalDateTime.ofEpochSecond(item.getTrackDateTime(), 0, zos);
            if (dtDb.getDayOfYear() == dt.getDayOfYear()) {
                return Optional.of(item);
            }
//            if (item.getTrackDateTime() == dt.toEpochSecond(zos)) {
//                return Optional.of(item);
//            }
        }
        return Optional.empty();
    }

    /**
     * utility method for external callers to determine who
     * cannot reauth with the refresh token
     * @param dc - DataCenterConfig
     * @return boolean - true means successful reauth
     */
    public boolean preFlightOAuth(DataCenterConfig dc) {
        OAuthCredentials creds;

        creds = dc.getOAuthCredentials();
        LocalDateTime dtNow = LocalDateTime.now();

        if (creds == null) {
            auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError, "Unsuccessful token refresh, credentials missing.");
            return false;
        }
        if (creds.isExpired()) {
            // need to refresh
            try {

                // if we can refresh token...
                creds = refreshToken(creds);
                dc.setCredentials(creds.toJson());
                dc.setOAuthCredentials(creds);

                // still expired? Unlikely, but we handle it just in case
                if (!creds.isExpired()) {
                    _logger.debug(String.format("Preflight success refreshing credentials for %s", dc.getFkUserGuid()));

                    // ...then we need to update the database as well
                    updateDataCenterConfigSuccess(dc, dtNow);
                    auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshedCredentials,
                            String.format("Refreshed OAuth2 credentials successfully for FitBit user '%s'", creds.getAccessUserId()));
                    return true;
                }
                else {
                    _logger.debug(String.format("Preflight FAILURE refreshing credentials for %s", dc.getFkUserGuid()));
                    auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError, "Unsuccessful token refresh, credentials expired.");
                    // we want to return false if the user would get an email, so if
                    // isInAuthWindow returns true, we send false to the caller
                    return !isInAuthWindow(dc);
                }
            }
            catch(IOException | ExecutionException | InterruptedException | RuntimeException ex) {
                _logger.debug(String.format("Preflight.Exception FAILURE refreshing credentials for %s", dc.getFkUserGuid()));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError, String.format("Unsuccessful token refresh: %s", ex.getMessage()));
                // we want to return false if the user would get an email, so if
                // isInAuthWindow returns true, we send false to the caller
                return !isInAuthWindow(dc);
            }
        }  // end if creds expired

        // if creds have not expired, they are good to go
        return true;
    }

    /**
     * helper method to refresh credentials
     * @param dc - DataCenterConfig
     * @param suppressNotifications - boolean
     * @return OAuthCredentials
     */
    private Optional<OAuthCredentials> checkRefresh(DataCenterConfig dc, boolean suppressNotifications) {
        return checkRefresh(dc, suppressNotifications, false);
    }

    /**
     * helper method to refresh credentials
     * @param dc - DataCenterConfig
     * @param suppressNotifications - boolean
     * @param isRetry - boolean, true if this is a retry attempt
     * @return OAuthCredentials
     */
    private Optional<OAuthCredentials> checkRefresh(DataCenterConfig dc, boolean suppressNotifications, boolean isRetry) {
        OAuthCredentials creds;
        creds = dc.getOAuthCredentials();
        LocalDateTime dtNow = LocalDateTime.now();

        if (creds == null) {
            // can't create them... fatal error for this user
            String msg = String.format("No OAuth credentials stored in database for user ID: %s", dc.getFkUserGuid());
            _logger.error(StringUtils.formatMessage(msg));
            updateDataCenterConfigError(dc, dtNow);
            auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError,
                    String.format("DataCentConfig has no credentials for '%s'", dc.getFkUserGuid()));
            return Optional.empty();
        }

        // we are now going to *always* refresh
        // if (creds.isExpired()) {

        try {
            updateDataCenterConfigDuringRefresh(dc, dtNow);

            // if we can refresh token...
            creds = refreshToken(creds);
            dc.setCredentials(creds.toJson());
            dc.setOAuthCredentials(creds);

            // still expired? Unlikely, but we handle it in the else just in case
            if (!creds.isExpired()) {
                // ...then we need to update the database as well
                updateDataCenterConfigSuccess(dc, dtNow);
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshedCredentials,
                        String.format("Refreshed OAuth2 credentials successfully for FitBit user '%s'", creds.getAccessUserId()));
            }
            else {
                updateDataCenterConfigError(dc, dtNow);
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.RefreshError, "Unsuccessful token refresh, credentials expired.");
                // send notification email if enabled
                if (!suppressNotifications) {
                    notifyUser(dc);
                }
            }
        }
        catch(IOException | ExecutionException | InterruptedException | RuntimeException ex) {
            if (!isRetry) {
                return checkRefresh(dc, suppressNotifications, true);
            }
            else {
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
        }
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
        LocalDateTime dtEnd = LocalDateTime.now();
        LocalDateTime dtStart = dtEnd.minusDays(_config.getFitbitQueryWindow());
        OAuthCredentials creds = dc.getOAuthCredentials();
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, suppressReauth);
        if (optCreds.isPresent()) {
            return getSteps(dc, dtStart, dtEnd);
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
        LocalDateTime dtEnd = LocalDateTime.now();
        LocalDateTime dtStart = dtEnd.minusDays(_config.getFitbitQueryWindow());
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, suppressReauth);
        if (optCreds.isPresent()) {
            return getWeight(dc, dtStart, dtEnd);
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
        LocalDateTime dtEnd = LocalDateTime.now();
        LocalDateTime dtStart = dtEnd.minusDays(_config.getFitbitQueryWindow());
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, suppressReauth);
        if (optCreds.isPresent()) {
            return getActivityLevels(dc, dtStart, dtEnd);
        }
        throw new IOException("No OAuthCredentials available for " + fkUserId);
    }


    /**
     * getSteps returns FitBet steps data for a single day
     * @param dc - DataCenterConfig
     * @param dtStart - LocalDateTime
     * @param dtEnd - LocalDateTime
     * @return - StepsData
     * @throws IOException on API error
     */
    private StepsData getSteps(DataCenterConfig dc, LocalDateTime dtStart, LocalDateTime dtEnd)
             throws IOException, ExecutionException, InterruptedException {

        DynaUrl urlInfo = prepUrl(_config.getFitbitStepsUrl(), dtStart, dtEnd);
        StepsData apiResults = (StepsData) makeApiRequest(StepsData.class, urlInfo.getUrl(), dc.getOAuthCredentials());

        List<SimpleTrack> dbResults = _trackService.findByUserTrackDateRange(dc.getFkUserGuid(), urlInfo.getStartDate(), urlInfo.getEndDate(), SimpleTrack.Entity.STEPS);

        for(Steps steps : apiResults.getSteps()) {

            Optional<SimpleTrack> result = findStepsMatch(steps, dbResults);
            SimpleTrack newTracker = new SimpleTrack(steps, dc.getFkUserGuid());
            if (result.isPresent()) {
                SimpleTrack dbSteps = result.get();
                newTracker.setSimpleTrackGuid(dbSteps.getSimpleTrackGuid());
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
        LocalDateTime dtExpired = dc.getOAuthCredentials().getExpirationDateTime();
        LocalDateTime dtChecked = dc.getLastChecked();
        if (dtExpired.isBefore(dtChecked)) {
            dtChecked = dtExpired;
        }

        // calculate earliest allowable date
        LocalDateTime dtStart = LocalDateTime.now().minusDays(_config.getFitbitReauthWindowDays());
        boolean inWindow = dtChecked.isAfter(dtStart);

        _logger.debug(String.format("isInAuthWindow - user %s, lastChecked: %s, dtExpired: %s, dtChecked: %s, dtStart: %s, inWindow: %s",
                                                    dc.getFkUserGuid(), dc.getLastChecked(), dtExpired, dtChecked, dtStart, inWindow));
        // is the date after the beginning of our window?
        return inWindow;
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

                Optional<User> optUser = _userService.findById(dc.getFkUserGuid());
                if (optUser.isPresent()) {

                    User user = optUser.get();
                    String email = user.getEmail();
                    String fname = "20/20 Lifestyles User";

                    // try to get better data from related Client row
                    if (!StringUtils.isNullOrEmpty(user.getFkClientId())) {
                        try {
                            int clientId = Integer.valueOf(user.getFkClientId());
                            Optional<Client> optClient = _clientService.findById(clientId);
                            if (optClient.isPresent()) {
                                Client client = optClient.get();
                                fname = client.getFname();
                                // override with email in Client if we have it
                                email = client.getEmail();
                            }
                        }
                        catch (NumberFormatException ex) {
                            String msg = String.format("Illegal non-integer value in User.fkClientId: %s, for user %s", user.getFkClientId(), dc.getFkUserGuid());
                            _logger.error(StringUtils.formatError(msg, ex));
                        }
                    }

                    // must have an email address to continue
                    if (!StringUtils.isNullOrEmpty(email)) {

                        // notify user with email (emailService logs send errors)
                        try {
                            _emailService.sendTemplatedEmail(email, fname);

                            String msg = StringUtils.formatMessage(String.format("Sent auth email to '%s'for user '%s'", email, dc.getFkUserGuid()));
                            _logger.info(StringUtils.formatMessage(msg));
                            auditEvent(dc.getFkUserGuid(), AuditLog.Activity.ReauthEmail, msg);
                        }
                        catch (IOException ex) {
                            String msg = String.format("Email server error while sending to %s", user.getEmail());
                            _logger.error(StringUtils.formatError(msg, ex));
                            auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, msg + " " + ex.getMessage());
                        }
                        catch (MessagingException ex) {
                            String msg = String.format("Email server authentication or connection error while sending to %s", user.getEmail());
                            _logger.error(StringUtils.formatError(msg, ex));
                            auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, msg + " " + ex.getMessage());
                        }
                    }
                    else {
                        // however unlikely, we should at least log not having an email addy
                        String msg = String.format("No email address available for userId: %s", dc.getFkUserGuid());
                        _logger.error(msg);
                        auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, msg);
                    }
                }
                else {
                    String msg = String.format("Notify(): User '%s' not found in User table.", dc.getFkUserGuid());
                    _logger.error(msg);
                    auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, msg);
                }
            }
        }
    }

    /**
     * helper method to update DataCenterConfig row
     * @param dc - DataCenterConfig
     */
    private void updateDataCenterConfigError(DataCenterConfig dc, LocalDateTime dt) {
        dc.setStatus(DataCenterConfig.PartnerStatus.RefreshErr.status);
        dc.setLastChecked(dt);
        dc.setStatusText(String.format("Refresh Error for day: %s", dt.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        dc.setModified(dt);
        _dcService.updateDataCenterConfig(dc);
    }

    /**
     * mark a DataCenterConfig as a refresh in progress
     * @param dc - DataCenterconfig
     */
    private void updateDataCenterConfigDuringRefresh(DataCenterConfig dc, LocalDateTime dt) {
        dc.setStatus(DataCenterConfig.PartnerStatus.AuthErr.status);
        dc.setStatusText("Refresh in progress");
        dc.setLastChecked(dt);
        dc.setModified(dt);
        _dcService.updateDataCenterConfig(dc);
    }

    /**
     * helper method to update the DataCenterConfig row after a successful
     * authorization with the FitBit API
     * @param dc - DataCenterConfig
     */
    private void updateDataCenterConfigSuccess(DataCenterConfig dc, LocalDateTime dt) {
        dc.setStatus(DataCenterConfig.PartnerStatus.Active.status);
        dc.setLastChecked(dt);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, YYYY - hh:mm:ss a");
        dc.setStatusText(dt.format(fmt));
        dc.setModified(dt);
        _dcService.updateDataCenterConfig(dc);
    }


    /**
     * gets all available FitBit data for a single user
     * @param userId - UUID
     * @param suppressNotifications - boolean
     * @throws IOException on internal API calls
     */
    public void processUser(UUID userId, LocalDateTime dtStart, LocalDateTime dtEnd, boolean suppressNotifications) throws IOException {
        Optional<DataCenterConfig> optDc = _dcService.findById(userId.toString(), SimpleTrack.SourceSystem.FITBIT.sourceSystem);
        if (optDc.isPresent()) {
            DataCenterConfig dc = optDc.get();
            processAll(dc, dtStart, dtEnd, suppressNotifications);
        }
        else {
            throw new IllegalArgumentException(String.format("User %s not found in DataCenterConfig table", userId));
        }
    }


    public void processAll(DataCenterConfig dc, LocalDateTime dtNow, LocalDateTime dtEnd) throws IOException {
        processAll(dc, dtNow, dtEnd,false);
    }

    /**
     * entry point for grabbing all available FitBit data for
     * a given user
     * @param dc - DataCenterConfig entry for use
     * @throws IOException on internal API call errors
     */
    public void processAll(DataCenterConfig dc, LocalDateTime dtStart, LocalDateTime dtEnd, boolean suppressNotifications) throws IOException {

        // check if they're valid, and if not, see if we can refresh
        Optional<OAuthCredentials> optCreds = checkRefresh(dc, suppressNotifications);
        if (!optCreds.isPresent()) {
            // all error handling and notifications occurred upstream
            return;
        }
        OAuthCredentials creds = optCreds.get();

        // if creds are not expired, our refresh was good, so
        // we can move forward with pulling data. If they ARE
        // expired, we fall out the bottom and exit.

        if (!creds.isExpired()) {
            // we've valid got credentials, so let's fetch us some ActivityLevel data
            try {
                getActivityLevels(dc, dtStart, dtEnd);
                _logger.info(StringUtils.formatMessage(String.format("ActivityLevelData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.ActivityLevelRead, "Successfully updated ActivityLevels");
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving ActivityLevelData for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating ActivityLevels: %s", ex.getMessage()));
            }

            // now fetch and save Sleep data
            try {
                getSleep(dc, dtStart, dtEnd);
                _logger.info(StringUtils.formatMessage(String.format("SleepData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.SleepRead, "Successfully updated Sleep data");
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Sleep data for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating Sleep: %s", ex.getMessage()));
            }

            // fetch and save Steps data
            try {
                getSteps(dc, dtStart, dtEnd);
                _logger.info(StringUtils.formatMessage(String.format("StepsData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.StepsRead, "Successfully updated Steps data");
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Steps data for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating Steps data: %s", ex.getMessage()));
            }


            // fetch and save Weight data
            try {
                getWeight(dc, dtStart, dtEnd);
                _logger.info(StringUtils.formatMessage(String.format("WeightData saved for %s", dc.getFkUserGuid())));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.WeightRead, "Successfully updated Weight data");
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Weight data for user '%s'", dc.getFkUserGuid()), ex));
                auditEvent(dc.getFkUserGuid(), AuditLog.Activity.Error, String.format("Error updating Weight data: %s", ex.getMessage()));
            }

            updateDataCenterConfigSuccess(dc, LocalDateTime.now());

        } // end if not expired credentials
    }
}
