package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.apis.fitbit.FitBitOAuth2AccessToken;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.proclub.datareader.config.AppConfig;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class FitBitDataService {

    // ----------------------------- static class data

    private static Logger _logger = LoggerFactory.getLogger(FitBitDataService.class);

    // This is effectively a cache of user credentials
    private static ConcurrentHashMap<UUID, OAuthCredentials> _authMap = new ConcurrentHashMap<>();


    // ----------------------------- instance data

    //get current TimeZone using getTimeZone method of Calendar class
    private static TimeZone _timeZone = Calendar.getInstance().getTimeZone();

    private AppConfig _config;
    private final OAuth20Service _oauthService;

    private OkHttpClient _client = new OkHttpClient();
    private ObjectMapper _mapper = new ObjectMapper();




    /**
     * ctor
     *
     * @param config - AppConfig
     */
    @Autowired
    public FitBitDataService(AppConfig config) {
        _config = config;
        _oauthService = new ServiceBuilder(_config.getFitbitClientId())
                .apiSecret(_config.getFitbitClientSecret())
                .scope(_config.getFitbitScope())
                .callback(_config.getFitbitCallbackUrl())
                .build(FitbitApi20.instance());
    }

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

    public void putOAuthCredentials(UUID userGuid, OAuthCredentials creds) {
        _authMap.put(userGuid, creds);
    }

    /**
     * private helper method to extract JSON string from responses
     * @param response - Response
     * @return String - JSON result
     * @throws IOException
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
     * @param dt
     * @return
     */
    private FitBitApiData makeApiRequest(Class dataClass, String baseUrl, OAuthCredentials oauth, Instant dt)
            throws IOException, ExecutionException, InterruptedException {

        LocalDateTime locDt = LocalDateTime.ofInstant(dt, _timeZone.toZoneId());
        String dtStr = locDt.format(DateTimeFormatter.ISO_DATE);  // YYYY-MM-DD
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
    public ActivityLevelData getActivityLevels(OAuthCredentials oauth, Instant dt)
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
    public WeightData getWeight(OAuthCredentials oauth, Instant dt)
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
    public SleepData getSleep(OAuthCredentials oauth, Instant dt)
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
    public StepsData getSteps(OAuthCredentials oauth, Instant dt)
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
}
