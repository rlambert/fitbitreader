package com.proclub.datareader.services;

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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
@SuppressWarnings("unused")
public class FitBitDataService {

    // ----------------------------- static class data

    private static Logger _logger = LoggerFactory.getLogger(FitBitDataService.class);

    // This is effectively a cache of user credentials
    private static ConcurrentHashMap<UUID, OAuthCredentials> _authMap = new ConcurrentHashMap<>();


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
                             ClientService clientService, EmailService emailService) {
        _config = config;
        _userService = userService;
        _dcService = dcService;
        _activityLevelService = activityLevelService;
        _trackService = trackService;
        _clientService = clientService;
        _emailService = emailService;

        // we need the config instance to build the library object,
        // so we instantiate the service object here
        _oauthService = new ServiceBuilder(_config.getFitbitClientId())
                .apiSecret(_config.getFitbitClientSecret())
                .scope(_config.getFitbitScope())
                .callback(_config.getFitbitCallbackUrl())
                .build(FitbitApi20.instance());
    }

    // ----------------------------- methods

    /**
     * get a user's OAuth creds from the cache
     * @param userGuid - UUID
     * @return Optional<OAuthCredentials>
     */
    public Optional<OAuthCredentials> getOAuthCredentials(UUID userGuid) {
        if (_authMap.containsKey(userGuid)) {
            return Optional.of(_authMap.get(userGuid));
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * adds or updates an OAuthCredentials object for a user.
     * @param userGuid - UUID
     * @param creds - OAuthCredentials
     */
    public void putOAuthCredentials(UUID userGuid, OAuthCredentials creds) {
        _authMap.put(userGuid, creds);
    }

    /**
     * remove an item from the cache.
     * @param userGuid - UUID
     */
    public void deleteOAuthCredentials(UUID userGuid) {
        _authMap.remove(userGuid);
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
                json = responseBody.toString();
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
    OAuthCredentials getAuth(String code) throws InterruptedException, ExecutionException, IOException, IllegalArgumentException {
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
    public OAuthCredentials refreshToken(String refreshToken) throws IOException, ExecutionException, InterruptedException {
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
    public OAuthCredentials refreshToken(OAuthCredentials oldCreds) throws IOException, ExecutionException, InterruptedException {
        return refreshToken(oldCreds.getRefreshToken());
    }

    /**
     * helper method to build an API request
     * @param oauth
     * @param dt - LocalDateTime
     * @return
     */
    private FitBitApiData makeApiRequest(Class dataClass, String baseUrl, OAuthCredentials oauth, LocalDateTime dt)
            throws IOException, ExecutionException, InterruptedException {

        //LocalDateTime locDt = LocalDateTime.ofInstant(dt, _timeZone.toZoneId());
        String dtStr = dt.format(DateTimeFormatter.ISO_DATE);  // YYYY-MM-DD
        String url = baseUrl.replace("${date}", dtStr);
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + oauth.getAccessToken())
                .header("Accept-Language", "en_US")
                .build();

        FitBitApiData objData = null;
        Response response = null;
        try {
            response = _client.newCall(req).execute();
            if (!response.isSuccessful()) {
                if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                    // reauthorize (will throw an exception if it fails)
                    oauth = refreshToken(oauth);
                    return makeApiRequest(dataClass, baseUrl, oauth, dt);
                }
            }
            else {
                String json = processResponseBody(response);
                objData = (FitBitApiData) _mapper.readValue(json, dataClass);
            }
        }
        catch (IOException | ExecutionException | InterruptedException ex) {
            _logger.error(StringUtils.formatError(ex));
            throw ex;
        }
        finally {
            if (response != null) {
                response.close();
            }
        }
        return objData;
    }

    /**
     * helper method to find an existing ActivityLevel
     * @param fitbitData
     * @param dbActivityLevels
     * @return
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
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - SleepData
     * @throws IOException
     */
    public List<ActivityLevel> getActivityLevels(DataCenterConfig dc, OAuthCredentials oauth, LocalDateTime dt)
                    throws IOException, ExecutionException, InterruptedException {
        LocalDateTime dtMidnight = LocalDateTime.of(dt.getYear(), dt.getMonth(), dt.getDayOfMonth(), 0, 0, 0, 0);

        // go back in time a configurable number of days
        LocalDateTime dtStart = dtMidnight.minusDays(_config.getFitbitActivityLevelWindowDays());
        String baseUrl = _config.getFitbitActivityUrl();

        // now iterate through each day in window
        LocalDateTime dtloop = dtStart;
        LocalDateTime dtEnd = dt.plusDays(1);

        // these are in TrackDateOrder
        List<ActivityLevel> dbActivityLevels = _activityLevelService.findByUserAndTrackDateWindow(dc.getFkUserGuid(), dtStart, dtEnd);

        List<ActivityLevel> apiResults = new ArrayList<>();

        while (dtloop.isBefore(dtEnd)) {
            // ask FitBit API for this day
            ActivityLevelData data = (ActivityLevelData) makeApiRequest(ActivityLevelData.class, baseUrl, oauth, dtloop);
            if (data != null) {
                // convert to our DTO
                ActivityLevel activityLevel = new ActivityLevel(dc.getFkUserGuid(), dtloop, data);
                Optional<ActivityLevel> optMatch = findActivityLevelMatch(activityLevel, dbActivityLevels);
                if (optMatch.isPresent()) {
                    ActivityLevel dbLevel = optMatch.get();
                    activityLevel.setActivityLevelId(dbLevel.getActivityLevelId());
                }

                // this is an insert (if no match already in DB) or update
                activityLevel = _activityLevelService.saveActivityLevel(activityLevel);
                apiResults.add(activityLevel);
                _logger.info(StringUtils.formatMessage(String.format("ActivityLevelData saved for %s - %s", dtloop.toString(), dc.getFkUserGuid())));
                dtloop.plusDays(1);
            }
            else {
                _logger.info(StringUtils.formatMessage(String.format("No ActivityLevelData available for %s - %s", dtloop.toString(), dc.getFkUserGuid())));
            }
        }
        return apiResults;
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
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - SleepData
     * @throws InterruptedException - not normal
     * @throws ExecutionException - also not likely
     * @throws IOException - if FitBit.com API is down
     */
    public WeightData getWeight(DataCenterConfig dc, OAuthCredentials oauth, LocalDateTime dt) throws IOException, ExecutionException, InterruptedException {
        String dtStr = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // the URL already has the date range embedded into the URL
        String baseUrl = _config.getFitbitWeightUrl().replace("${date}", dtStr);

        // go back in time a configurable number of days
        LocalDateTime dtStart = dt.minusDays(_config.getFitbitWeightWindowDays());
        LocalDateTime dtEnd   = dt;
        List<SimpleTrack> dbResults = _trackService.findByUserTrackDateRange(dc.getFkUserGuid(), dtStart, dtEnd, SimpleTrack.Entity.WEIGHT);

        WeightData apiResults = (WeightData) makeApiRequest(WeightData.class, baseUrl, oauth, dt);
        for(Weight wt : apiResults.getWeight()) {
            Optional<SimpleTrack> result = findWeightMatch(wt, dbResults);
            SimpleTrack newTracker = new SimpleTrack(wt);
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
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - SleepData
     * @throws IOException
     */
    public SleepData getSleep(DataCenterConfig dc, OAuthCredentials oauth, LocalDateTime dt) throws IOException, ExecutionException, InterruptedException {

        LocalDateTime dtStart = dt.minusDays(_config.getFitbitSleepWindowDays());
        LocalDateTime dtEnd   = dt.plusDays(1);
        LocalDateTime dtLoop  = dtStart;

        // get all API data for our date-range
        String dtStartStr = dtStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dtEndStr = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));         // original date is end of range

        // https://api.fitbit.com/1.2/user/-/sleep/date/${startDate}/${endDate}.json
        String baseUrl = _config.getFitbitSleepUrl().replace("${startDate}", dtStartStr).replace("${endDate}", dtEndStr);
        SleepData sleepData = (SleepData) makeApiRequest(SleepData.class, baseUrl, oauth, dt);

        // get all database rows that overlap; go back in time a configurable number of days
        List<SimpleTrack> dbResults = _trackService.findByUserTrackDateRange(dc.getFkUserGuid(), dtStart, dtEnd, SimpleTrack.Entity.SLEEP);

        for(Sleep sleep : sleepData.getSleep()) {
            Optional<SimpleTrack> result = findSleepMatch(sleep, dbResults);
            SimpleTrack newTracker = new SimpleTrack(sleep);
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
     * getSteps returns FitBet steps data for a single day
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - StepsData
     * @throws IOException
     */
    public StepsData getSteps(DataCenterConfig dc, OAuthCredentials oauth, LocalDateTime dt)
                throws IOException, ExecutionException, InterruptedException {

        String dtStr = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String baseUrl = _config.getFitbitStepsUrl().replace("${date}", dtStr);

        SleepData sleep = (SleepData) makeApiRequest(SleepData.class, baseUrl, oauth, dt);

        // go back in time a configurable number of days
        LocalDateTime dtStart = dt.minusDays(_config.getFitbitStepsWindowDays());
        LocalDateTime dtEnd   = dt;
        List<SimpleTrack> dbResults = _trackService.findByUserTrackDateRange(dc.getFkUserGuid(), dtStart, dtEnd, SimpleTrack.Entity.STEPS);

        StepsData apiResults = (StepsData) makeApiRequest(StepsData.class, baseUrl, oauth, dt);
        for(Steps steps : apiResults.getSteps()) {
            Optional<SimpleTrack> result = findStepsMatch(steps, dbResults);
            SimpleTrack newTracker = new SimpleTrack(steps);
            if (result.isPresent()) {
                SimpleTrack dbWt = result.get();
                newTracker.setSimpleTrackGuid(dbWt.getSimpleTrackGuid());
            }
            // this will update an existing record if there is an ID
            _trackService.createSimpleTrack(newTracker);
        }

        return (StepsData) makeApiRequest(StepsData.class, baseUrl, oauth, dt);

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
     * helper method to notify users via email if they need to reauthorize
     * the application
     * @param dc - DataCenterConfig
     */
    private void notifyUser(DataCenterConfig dc) {
        // handle an auth error: is authorization email enabled?
        if (_config.isFitbitSendAuthEmail()) {
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
                    }
                    else {
                        Client client = clientList.get(0);
                        fname = client.getFname();
                    }

                    // notify user with email (emailService logs send errors)
                    _emailService.sendTemplatedEmail(user.getEmail(), fname);
                    _logger.info(StringUtils.formatMessage(String.format("Sent auth email to '%s'for user '%s'", user.getEmail(), dc.getFkUserGuid())));
                }
                else {
                    // however unlikely, we should at least log not having an email addy
                    _logger.error(String.format("No email address in User table for userId: %s", dc.getFkUserGuid()));
                }
            }
            else {
                _logger.error(String.format("User '%s' not found in User table.", dc.getFkUserGuid().toString()));
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
     *
     * @param dc
     * @param dt
     */
    private void updateDataCenterConfigSuccess(DataCenterConfig dc, LocalDateTime dt) {
        dc.setStatus(DataCenterConfig.PartnerStatus.Active.status);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, YYYY - hh:mm:ss a");
        dc.setStatusText(dt.format(fmt));
        dc.setLastChecked(dt);
        _dcService.updateDataCenterConfig(dc);
    }

    /**
     * entry point for grabbing all available FitBit data for
     * a given user
     * @param dc - DataCenterConfig entry for use
     * @throws IOException
     */
    public void processAll(DataCenterConfig dc, LocalDateTime dtNow) throws IOException {
        OAuthCredentials creds;

        // do we already have the credentials in the cache?
        if (!_authMap.containsKey(dc.getFkUserGuid())) {

            // no, so see if we can create them
            if (StringUtils.isNullOrEmpty(dc.getCredentials())) {

                // can't create them... fatal error for this user
                String msg = String.format("No OAuth credentials stored in database for user ID: %s", dc.getFkUserGuid().toString());
                _logger.error(StringUtils.formatMessage(msg));

                // send notification email if enabled
                notifyUser(dc);
                updateDataCenterConfigError(dc, dtNow);
                return;
            }
            else {
                // we have OAuth credentials
                creds = OAuthCredentials.create(dc.getCredentials());
            }
        }
        else {
            creds = _authMap.get(dc.getFkUserGuid());
        }

        // update or add to cache
        _authMap.put(dc.getFkUserGuid(), creds);

        if (creds.isExpired()) {
            // need to refresh
            try {
                updateDataCenterConfigDuringRefresh(dc, dtNow);
                // if we can refresh token...
                creds = refreshToken(creds);
                if (creds != null) {
                    // ...then we need to update the database as well
                    dc.setCredentials(creds.toJson());
                    updateDataCenterConfigSuccess(dc, dtNow);
                }
            }
            catch(IOException | ExecutionException | InterruptedException ex) {
                String msg = String.format("Could not refresh OAuth2 token for user '%s'", dc.getFkUserGuid());
                _logger.error(msg, ex);
                // send notification email if enabled
                notifyUser(dc);
                updateDataCenterConfigError(dc, dtNow);
            }
        }  // end if creds expired

        // if creds are not expired, our refresh was good, so
        // we can move forward with pulling data. If they ARE
        // expired, we fall out the bottom and exit.

        if (!creds.isExpired()) {
            // we've valid got credentials, so let's fetch us some ActivityLevel data
            try {
                getActivityLevels(dc, creds, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("ActivityLevelData saved for %s", dc.getFkUserGuid())));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving ActivityLevelData for user '%s'", dc.getFkUserGuid()), ex));
            }

            // now fetch and save Sleep data
            try {
                getSleep(dc, creds, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("SleepData saved for %s", dc.getFkUserGuid())));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Sleep data for user '%s'", dc.getFkUserGuid()), ex));
            }

            // fetch and save Steps data
            try {
                getSteps(dc, creds, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("StepsData saved for %s", dc.getFkUserGuid())));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Steps data for user '%s'", dc.getFkUserGuid()), ex));
            }


            // fetch and save Weight data
            try {
                getWeight(dc, creds, dtNow);
                _logger.info(StringUtils.formatMessage(String.format("WeightData saved for %s", dc.getFkUserGuid())));
            }
            catch(InterruptedException | ExecutionException ex) {
                _logger.error(StringUtils.formatError(String.format("Error saving Weight data for user '%s'", dc.getFkUserGuid()), ex));
            }
        } // end if not expired credentials
    }
}
