package com.proclub.datareader.dao;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table(name="User")
public class User {

    @Id
    private UUID userGuid;
    private int modifiedDateTime;
    private int clientType;
    private String email;
    private String fkUserStore2020;
    private String fkUserStorePRO;
    private String postalCode;
    private String fkClientId;

    /*
        [UserGuid] [uniqueidentifier] NOT NULL,
    [ModifiedDateTime] [int] NOT NULL,
    [ClientType] [int] NOT NULL,
    [Email] [varchar](100) NULL,
    [fkUserStore2020] [varchar](50) NULL,
    [fkUserStorePRO] [varchar](50) NULL,
    [PostalCode] [nvarchar](12) NULL,
    [fkClientId] [varchar](200) NULL,
     */

    public User() {}

    public User(UUID userGuid, int modifiedDateTime, int clientType, String email, String fkUserStore2020, String fkUserStorePRO, String postalCode, String fkClientId) {
        this.userGuid = userGuid;
        this.modifiedDateTime = modifiedDateTime;
        this.clientType = clientType;
        this.email = email;
        this.fkUserStore2020 = fkUserStore2020;
        this.fkUserStorePRO = fkUserStorePRO;
        this.postalCode = postalCode;
        this.fkClientId = fkClientId;
    }

}
