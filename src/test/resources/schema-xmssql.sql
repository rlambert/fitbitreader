/*
  This is for testing in Sql Server
 */

CREATE TABLE Users (
    UserGuid uniqueidentifier PRIMARY KEY NOT NULL,
    ModifiedDateTime int NOT NULL,
    ClientType int NOT NULL,
    Email varchar(100) NULL,
    fkUserStore2020 varchar(50) NULL,
    fkUserStorePRO varchar(50) NULL,
    PostalCode varchar(12) NULL,
    fkClientId varchar(200) NULL
);

CREATE TABLE Client (
    ClientId int NOT NULL PRIMARY KEY IDENTITY,
    ClientType int NOT NULL,
    Fname varchar(40) NULL,
    Lname varchar(60) NULL,
    Email varchar(254) NULL,
    Zip varchar(15) NULL,
    fkTrackerGuid varchar(200) NULL,
    fkUserStore2020 varchar(200) NULL,
    fkUserStorePRO varchar(200) NULL,
    fkGpId varchar(30) NULL,
    Logon varchar(254) NOT NULL,
    Pwd varchar(200) NULL,
    Status int NOT NULL,
    isCastMember bit NOT NULL default 0
);

CREATE TABLE DataCenterConfig(
    fkUserGuid uniqueidentifier NOT NULL,
    SourceSystem int NOT NULL,
    LastChecked datetime NOT NULL,
    PanelDisplay int NOT NULL,
    Status int NOT NULL,
    StatusText varchar(150) NULL,
    DataStatus int NOT NULL,
    Credentials varchar(700) NULL,
    Modified datetime NOT NULL
);

CREATE TABLE ActivityLevel(
    ActivityLevelID bigint IDENTITY(1,1) NOT NULL,
    fkUserGuid uniqueidentifier NOT NULL,
    ModifiedDateTime datetime NOT NULL,
    TrackDateTime datetime NOT NULL,
    FairlyActiveMinutes int NOT NULL,
    LightlyActiveMinutes int NOT NULL,
    VeryActiveMinutes int NOT NULL,
    DeviceReported bit NOT NULL
);

CREATE TABLE SimpleTrack(
    SimpleTrackGuid uniqueidentifier NOT NULL PRIMARY KEY,
    fkUserGuid uniqueidentifier NOT NULL,
    fkProviderId int NOT NULL,
    SourceSystem tinyint NOT NULL,
    ModifiedDateTime int NOT NULL,
    TrackDateTime int NOT NULL,
    EntityType tinyint NOT NULL,
    ValTinyInt tinyint NULL,
    ValInt int NULL,
    ValDec decimal(18, 4) NULL,
    ValStr varchar(200) NULL,
    Sync tinyint NOT NULL,
    ValInt2 int NULL,
    ValTime int NULL,
    ValTime2 int NULL,
    DeviceReported bit NOT NULL
);