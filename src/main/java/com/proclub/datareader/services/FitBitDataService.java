package com.proclub.datareader.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.config.AppConfig;
import com.proclub.datareader.model.security.OAuthCredentials;
import com.proclub.datareader.model.steps.StepsData;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class FitBitDataService {

    private static Logger _logger = LoggerFactory.getLogger(FitBitDataService.class);

    private AppConfig _config;

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
     * getSteps returns FitBet steps data for a single day
     * @param oauth - OAuthCredentials reference
     * @param dt - Instant
     * @return
     */
    public StepsData getSteps(OAuthCredentials oauth, Instant dt) throws IOException {
        LocalDateTime locDt = LocalDateTime.ofInstant(dt, ZoneOffset.UTC);
        String dstr = locDt.format(DateTimeFormatter.ofPattern("yyyy-MM-DD"));
        String url = _config.getFitbitStepsUrl().replace("${date}", dstr);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + oauth.getAccessToken())
                .header("Accept-Language", "en_US")
                .build();

        StepsData stepsData = null;
        try {
            Response response = _client.newCall(request).execute();
            String json = processResponseBody(response);
            stepsData = _mapper.readValue(json, StepsData.class);
            //SimpleTrack tracker = new SimpleTrack(stepsData);
            //_trackService.createSimpleTrack(tracker);
        }
        catch (IOException ex) {
            _logger.error(StringUtils.formatError(ex));
            throw ex;
        }

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
        return stepsData;
    }
}
