package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface UserRepo extends JpaRepository<User, UUID> {
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
    List<User> findByModifiedDateTimeAfter(int ts);

    List<User> findByModifiedDateTimeBetween(int tsStart, int tsEnd);

    List<User> findByEmail(String email);
}
