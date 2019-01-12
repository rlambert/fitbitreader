package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ClientRepo extends JpaRepository<Client, Integer> {
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
    List<Client> findByFkTrackerGuid(String guid);

    List<Client> findByLname(String lname);

    List<Client> findByFnameAndLname(String Fname, String Lname);

    List<Client> findByEmail(String email);
}
