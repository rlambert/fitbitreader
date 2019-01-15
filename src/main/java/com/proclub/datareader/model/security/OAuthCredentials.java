package com.proclub.datareader.model.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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

    /**
     * no arg ctor
     */
    public OAuthCredentials() {}

    /**
     * full-arg constructor
     * @param accessToken - String
     * @param accessSecret - String
     * @param refreshToken - String
     * @param expiresAt - String
     */
    public OAuthCredentials(String accessToken, String accessSecret, String refreshToken, String expiresAt) {
        this.accessToken = accessToken;
        this.accessSecret = accessSecret;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }
}
