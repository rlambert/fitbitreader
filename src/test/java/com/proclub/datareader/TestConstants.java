package com.proclub.datareader;

import java.time.Instant;
import java.util.UUID;

public class TestConstants {

    public static final UUID TEST_USER_GUID1 = UUID.randomUUID();
    public static final UUID TEST_USER_GUID2 = UUID.randomUUID();
    public static final UUID TEST_DEVICE_GUID1 = UUID.randomUUID();
    public static final UUID TEST_DEVICE_GUID2 = UUID.randomUUID();
    public static final String TEST_FKTRACKER_GUID1 = UUID.randomUUID().toString();
    public static final String TEST_FKTRACKER_GUID2 = UUID.randomUUID().toString();
    public static final String TEST_FKGRPID1 = "TestGroup1";
    public static final String TEST_FKGRPID2 = "TestGroup2";
    public static final String TEST_EMAIL1   = "fredf@bedrock.com";
    public static final String TEST_EMAIL2   = "wilmaf@bedrock.com";
    public static final String TEST_FKUSERTORE2020 = "fkUserStore2020";
    public static final String TEST_FKUSERSTOREPRO = "fkUserStorePRO";
    public static final String TEST_POSTAL_CODE1 = "98801";
    public static final String TEST_POSTAL_CODE2 = "98802";
    public static final String TEST_FKCLIENTID1 = "TestClientId1";
    public static final String TEST_FKCLIENTID2 = "TestClientId2";
    public static final int TEST_CLIENT_TYPE1   = 1;
    public static final int TEST_CLIENT_TYPE2   = 2;

    public static final String TEST_FNAME1 = "Fred";
    public static final String TEST_FNAME2 = "Barney";
    public static final String TEST_LNAME1 = "Flintstone";
    public static final String TEST_LNAME2 = "Rubble";

    public static final String TEST_LOGIN1 = "fredf";
    public static final String TEST_LOGIN2 = "barneyb";
    public static final String TEST_PWD1 = "fakehash1==";
    public static final String TEST_PWD2 = "fakehash2==";

    public static final UUID TEST_SESSION_TOKEN1 = UUID.randomUUID();
    public static final UUID TEST_SESSION_TOKEN2 = UUID.randomUUID();

    public static final int DAY1_SECONDS = 24 * 60 * 60;
    public static final int DAY2_SECONDS = DAY1_SECONDS * 2;
    public static final int DAY5_SECONDS = DAY1_SECONDS * 5;
    public static final int DAY10_SECONDS = DAY1_SECONDS * 10;
    public static final int DAY20_SECONDS = DAY1_SECONDS * 20;
    public static final int DAY30_SECONDS = DAY1_SECONDS * 30;

    public static final int REMEMBER_UNTIL1 = (int) (Instant.now().toEpochMilli()/1000 + DAY10_SECONDS);
    public static final int REMEMBER_UNTIL2 = (int) (Instant.now().toEpochMilli()/1000 + DAY20_SECONDS);

    public static final int TOKEN1_EXPIRES = (int) (Instant.now().toEpochMilli()/1000 + DAY20_SECONDS);
    public static final int TOKEN2_EXPIRES = (int) (Instant.now().toEpochMilli()/1000 + DAY30_SECONDS);

    public static final int SOURCE_FITBIT = 8;
    public static final int SOURCE_OTHER = 0;

    public static String TEST_CREDS = "{\"AccessToken\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyNUsyTUoiLCJhdWQiOiIyMjhRV0QiLCJpc3MiOiJGaXRiaXQiLCJ0eXAiOiJhY2Nlc3NfdG9rZW4iLCJzY29wZXMiOiJyd2VpIHJhY3QgcnNsZSIsImV4cCI6MTUxNTIyNzA3NSwiaWF0IjoxNTE1MTk4Mjc1fQ.Srde6nOYdecd2SeQCVoppK7cDE6d36d4WjI8zjPI_GQ\",\"AccessSecret\":\"OAuth2.0 not required\",\"AccessUserId\":\"25K2MJ\",\"RefreshToken\":\"e9400fba91a13f0f202593285c231f939fd8c6d418ab45c0e2601a431408566c\",\"ExpiresAt\":\"1/6/2018 12:23:35 AM\"}";

    public static final int TEST_FAIRLYACTIVE1 = 10;
    public static final int TEST_LIGHTLYACTIVE1 = 11;
    public static final int TEST_VERYACTIVE1 = 12;

    public static final int TEST_FAIRLYACTIVE2 = 20;
    public static final int TEST_LIGHTLYACTIVE2 = 21;
    public static final int TEST_VERYACTIVE2 = 22;
}
