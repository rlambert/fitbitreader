package com.proclub.datareader.dao;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name="Client")
public class Client {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int clientId;
    private int clientType;
    private String fname;
    private String lname;
    private String email;
    private String zip;

    @Column(name="fkTrackerGuid")
    private String fkTrackerGuid;
    @Column(name="fkUserStore2020")
    private String fkUserStore2020;
    @Column(name="fkUserStorePRO")
    private String fkUserStorePRO;
    @Column(name="fkGpId")
    private String fkGpId;

    private String logon;
    private String pwd;
    private int status;
    @Column(name="isCastMember")
    private boolean isCastMember;


    public Client() {}

    /**
     * full constructor
     * @param clientType
     * @param fname
     * @param lname
     * @param email
     * @param zip
     * @param fkTrackerGuid
     * @param fkUserStore2020
     * @param fkUserStorePRO
     * @param fkGpId
     * @param logon
     * @param pwd
     * @param status
     * @param isCastMember
     */
    public Client(int clientType, String fname, String lname, String email, String zip, String fkTrackerGuid, String fkUserStore2020, String fkUserStorePRO, String fkGpId, String logon, String pwd, int status, boolean isCastMember) {
        this.clientType = clientType;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.zip = zip;
        this.fkTrackerGuid = fkTrackerGuid;
        this.fkUserStore2020 = fkUserStore2020;
        this.fkUserStorePRO = fkUserStorePRO;
        this.fkGpId = fkGpId;
        this.logon = logon;
        this.pwd = pwd;
        this.status = status;
        this.isCastMember = isCastMember;
    }

    /*
    CREATE TABLE [dbo].[Client](
    [ClientId] [int] IDENTITY(0,1) NOT NULL,
    [ClientType] [int] NOT NULL,
    [Fname] [varchar](40) NULL,
    [Lname] [varchar](60) NULL,
    [Email] [varchar](254) NULL,
    [Zip] [varchar](15) NULL,
    [fkTrackerGuid] [varchar](200) NULL,
    [fkUserStore2020] [varchar](200) NULL,
    [fkUserStorePRO] [varchar](200) NULL,
    [fkGpId] [varchar](30) NULL,
    [Logon] [varchar](254) NOT NULL,
    [Pwd] [varchar](200) NULL,
    [Status] [int] NOT NULL,
    [isCastMember] [bit] NOT NULL,
CONSTRAINT [PK_Client] PRIMARY KEY NONCLUSTERED
     */


}
