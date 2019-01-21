package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.apis.fitbit.FitBitOAuth2AccessToken;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.dao.ActivityLevel;
import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.dao.User;
import com.proclub.datareader.model.FitBitApiData;
import com.proclub.datareader.model.activitylevel.ActivityLevelData;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.model.sleep.SleepData;
import com.proclub.datareader.model.steps.StepsData;
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
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
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
    private DataCenterConfigService _dcService;         // DataCenterConfig service
    private ActivityLevelService _activityLevelService; // get/put ActivityLevel
    private SimpleTrackService _trackService;           // get/put SimpleTrack data
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
                             ActivityLevelService activityLevelService, SimpleTrackService trackService) {
        _config = config;
        _userService = userService;
        _dcService = dcService;
        _activityLevelService = activityLevelService;
        _trackService = trackService;

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
            if (response.code() == HttpStatus.UNAUTHORIZED.value()) {
                // get new token with refresh_token
            }
            else {
                throw new IOException("Unexpected response code: " + response);
            }
        }
        else {
            ResponseBody responseBody = response.body();
            json = responseBody.toString();
            response.close();
        }
        return json;
    }

    /**
     * Once a user has clicked on the authorization URL and allowed our app,
     * we get called with their verify code. This method uses that code to
     * request an OAuth2 token, which is then returned in a JWT. That token
     * is converted to ProClub's tweaked OAuthCredentials object and returned.
     * @param code
     * @return OAuthCredentials
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     * @throws IllegalArgumentException
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
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public OAuthCredentials refreshToken(String refreshToken)
                throws IOException, ExecutionException, InterruptedException {
        OAuth2AccessToken token = _oauthService.refreshAccessToken(refreshToken);
        // the original token already had to have been a FitBit token or we
        // wouldn't be here
        return new OAuthCredentials((FitBitOAuth2AccessToken) token);
    }

    /**
     * convenience overload
     * @param oldCreds
     * @return OAuthCredentials
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public OAuthCredentials refreshToken(OAuthCredentials oldCreds)
                throws IOException, ExecutionException, InterruptedException {
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
     * getActivityLevels returns ActivityLevel data for a single day from the
     * FitBit API
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - SleepData
     * @throws IOException
     */
    public ActivityLevelData getActivityLevels(OAuthCredentials oauth, LocalDateTime dt)
                    throws IOException, ExecutionException, InterruptedException {
        // makeApiRequest(Class dataClass, String baseUrl, OAuthCredentials oauth, Instant dt)
        String baseUrl = _config.getFitbitActivityUrl();
        return (ActivityLevelData) makeApiRequest(ActivityLevelData.class, baseUrl, oauth, dt);
    }

    /**
     * getWeight returns weight data for a single day from the
     * FitBit API
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - SleepData
     * @throws IOException
     */
    public WeightData getWeight(OAuthCredentials oauth, LocalDateTime dt)
            throws IOException, ExecutionException, InterruptedException {
        String baseUrl = _config.getFitbitWeightUrl();
        return (WeightData) makeApiRequest(WeightData.class, baseUrl, oauth, dt);
    }

    /**
     * getSleep returns sleep data for a single day from the
     * FitBit API
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - SleepData
     * @throws IOException
     */
    public SleepData getSleep(OAuthCredentials oauth, LocalDateTime dt)
            throws IOException, ExecutionException, InterruptedException {
        String baseUrl = _config.getFitbitSleepUrl();
        return (SleepData) makeApiRequest(SleepData.class, baseUrl, oauth, dt);
    }

    /**
     * getSteps returns FitBet steps data for a single day
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return - StepsData
     * @throws IOException
     */
    public StepsData getSteps(OAuthCredentials oauth, LocalDateTime dt)
                throws IOException, ExecutionException, InterruptedException {
        String baseUrl = _config.getFitbitStepsUrl();
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
                    // TODO: Send FitBit Auth Email

                    _logger.info(StringUtils.formatMessage(String.format("Sent auth email to '%s'for user '%s'", user.getEmail(), dc.getFkUserGuid())));
                }
            }
            else {
                _logger.error(String.format("User '%s' not found in User table.", dc.getFkUserGuid().toString()));
            }
        }
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
            }
            else {
                // we have OAuth credentials
                creds = OAuthCredentials.create(dc.getCredentials());
                if (creds.isExpired()) {
                    // need to refresh
                    try {
                        // if we can refresh token...
                        creds = refreshToken(creds);
                        if (creds != null) {
                            // ...then we need to update the database as well
                            dc.setCredentials(creds.toJson());
                            _dcService.updateDataCenterConfig(dc);
                        }
                    }
                    catch(IOException | ExecutionException | InterruptedException ex) {
                        String msg = String.format("Could not refresh OAuth2 token for user '%s'", dc.getFkUserGuid());
                        _logger.error(msg, ex);
                        // send notification email if enabled
                        notifyUser(dc);
                    }
                }

                // we've got credentials, so let's fetch us some ActivityLevel data
                try {
                    ActivityLevelData aldata = getActivityLevels(creds, dtNow);
                    ActivityLevel activityLevel = new ActivityLevel(dc.getFkUserGuid(), dtNow, aldata);
                    _activityLevelService.createActivityLevel(activityLevel);
                }
                catch(InterruptedException | ExecutionException ex) {
                    _logger.error(StringUtils.formatError(String.format("Error saving ActivityLevelData for user '%s'", dc.getFkUserGuid()), ex));
                }

                // now fetch and save Sleep data
                try {
                    SleepData sleepData = getSleep(creds, dtNow);
                    if (sleepData.getSleep().size() > 0) {
                        SimpleTrack sleepTrack = new SimpleTrack(sleepData);
                        _trackService.createSimpleTrack(sleepTrack);
                    }
                    else {
                        _logger.info(StringUtils.formatMessage(String.format("No sleep data for user '%s' at this time.", dc.getFkUserGuid())));
                    }
                }
                catch(InterruptedException | ExecutionException ex) {
                    _logger.error(StringUtils.formatError(String.format("Error saving Sleep data for user '%s'", dc.getFkUserGuid()), ex));
                }

                // fetch and save Steps data
                try {
                    StepsData stepsData = getSteps(creds, dtNow);
                    if (stepsData.getSteps().size() > 0) {
                        SimpleTrack sleepTrack = new SimpleTrack(stepsData);
                        _trackService.createSimpleTrack(sleepTrack);
                    }
                    else {
                        _logger.info(StringUtils.formatMessage(String.format("No Steps data for user '%s' at this time.", dc.getFkUserGuid())));
                    }
                }
                catch(InterruptedException | ExecutionException ex) {
                    _logger.error(StringUtils.formatError(String.format("Error saving Steps data for user '%s'", dc.getFkUserGuid()), ex));
                }


                // fetch and save Weight data
                try {
                    WeightData wtData = getWeight(creds, dtNow);
                    if (wtData.getWeight().size() > 0) {
                        SimpleTrack wtTrack = new SimpleTrack(wtData);
                        _trackService.createSimpleTrack(wtTrack);
                    }
                    else {
                        _logger.info(StringUtils.formatMessage(String.format("No Weight data for user '%s' at this time.", dc.getFkUserGuid())));
                    }
                }
                catch(InterruptedException | ExecutionException ex) {
                    _logger.error(StringUtils.formatError(String.format("Error saving Weight data for user '%s'", dc.getFkUserGuid()), ex));
                }

            }
        }
        else {
            creds = _authMap.get(dc.getFkUserGuid());
        }

    }
}
