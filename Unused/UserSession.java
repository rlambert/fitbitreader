package com.proclub.datareader.dao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table(name="UserSession")
public class UserSession {

    @Id
    @Column(name = "SessionToken")
    private UUID sessionToken;
    @Column(name = "RememberUntil")
    private int rememberUntil;
    @Column(name = "DatabaseVersion")
    private String databaseVersion;
    @Column(name = "SessionTokenExpiresDate")
    private int sessionTokenExpiresDate;
    private UUID fkUserGuid;
    private UUID fkDeviceGuid;
    /*
        [RememberUntil] [int] NULL,
    [SessionToken] [uniqueidentifier] NOT NULL,
    [DatabaseVersion] [varchar](50) NOT NULL,
    [SessionTokenExpiresDate] [int] NOT NULL,
    [fkUserGuid] [uniqueidentifier] NOT NULL,
    [fkDeviceGuid] [uniqueidentifier] NOT NULL
     */

    public UserSession() {}

    public UserSession(UUID sessionToken, int rememberUntil, String databaseVersion, int sessionTokenExpiresDate, UUID fkUserGuid, UUID fkDeviceGuid) {
        this.sessionToken = sessionToken;
        this.rememberUntil = rememberUntil;
        this.databaseVersion = databaseVersion;
        this.sessionTokenExpiresDate = sessionTokenExpiresDate;
        this.fkUserGuid = fkUserGuid;
        this.fkDeviceGuid = fkDeviceGuid;
    }
}
