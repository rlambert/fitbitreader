package com.proclub.datareader.model.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.scribejava.apis.fitbit.FitBitOAuth2AccessToken;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Data
@JsonIgnoreProperties
public class OAuthCredentials {

    @JsonProperty("AccessToken")
    private String accessToken;

    @JsonProperty("AccessSecret")
    private String accessSecret;

    @JsonProperty("AccessUserId")
    private String accessUserId;

    @JsonProperty("RefreshToken")
    private String refreshToken;

    @JsonProperty("ExpiresAt")
    private String expiresAt;

    @JsonIgnore
    private String fitbitExpiresAtFormat = "M/d/yyyy hh:mm:ss a";

    /**
     * no arg ctor
     */
    public OAuthCredentials() {}

    /**
     * constructor for FitbitAccessToken
     * @param token - FitBitOAuth2AccessToken
     */
    public OAuthCredentials(FitBitOAuth2AccessToken token) {
        this.accessToken    = token.getAccessToken();
        this.accessSecret   = "OAuth2.0 not required";
        this.refreshToken   = token.getRefreshToken();
        LocalDateTime dt    = LocalDateTime.now().plus(token.getExpiresIn(), ChronoUnit.MILLIS);
        // 1/16/2019 4:17:40 PM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.fitbitExpiresAtFormat);
        this.expiresAt      = dt.format(formatter);
        this.accessUserId   = token.getUserId();
    }
}
