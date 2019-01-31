package com.proclub.datareader.model.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.fitbit.FitBitOAuth2AccessToken;
import com.proclub.datareader.utils.StringUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Data
@JsonIgnoreProperties
public class OAuthCredentials {

    private static ObjectMapper _mapper = new ObjectMapper();
    private static Logger _logger = LoggerFactory.getLogger(OAuthCredentials.class);

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
    private String fitbitExpiresAtFormat = "M/d/yyyy h:mm:ss a";


    @JsonIgnore
    private LocalDateTime expirationDt;


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
        LocalDateTime dt    = LocalDateTime.now().plus(token.getExpiresIn(), ChronoUnit.SECONDS);
        this.expirationDt   = dt;
        // 1/16/2019 4:17:40 PM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.fitbitExpiresAtFormat);
        this.expiresAt      = dt.format(formatter);
        this.accessUserId   = token.getUserId();
    }

    /**
     * returns expiration date as a LocalDateTime calculated
     * from the expiresAt field
     * @return LocalDateTime
     */
    @JsonIgnore
    public LocalDateTime getExpirationDateTime() {
        if (this.expirationDt == null) {
            if (!StringUtils.isNullOrEmpty(this.expiresAt)) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(this.fitbitExpiresAtFormat);
                this.expirationDt = LocalDateTime.parse(this.expiresAt, fmt);
            }
            else {
                _logger.error(String.format("expiresAt is null in OAuthCredentials for FitBit user: %s", this.accessUserId));
            }
        }
        return this.expirationDt;
    }

    /**
     * returns true if the OAuth token is expired
     * @return boolean
     */
    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.getExpirationDateTime());
    }

    /**
     * static builder method to create an instance from the JSON stored
     * in the database (which is not the original JWT for some reason)
     * @param dbCreds - String
     * @return OAuthCredentials
     * @throws IOException
     */
    public static OAuthCredentials create(String dbCreds) throws IOException {
        return _mapper.readValue(dbCreds, OAuthCredentials.class);
    }

    /**
     * utility method to produce JSON from an instance
     * @return String
     * @throws JsonProcessingException
     */
    public String toJson() throws JsonProcessingException {
        return _mapper.writeValueAsString(this);
    }
}
