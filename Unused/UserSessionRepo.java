package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface UserSessionRepo extends JpaRepository<UserSession, UUID> {
    /*
        [RememberUntil] [int] NULL,
    [SessionToken] [uniqueidentifier] NOT NULL,
    [DatabaseVersion] [varchar](50) NOT NULL,
    [SessionTokenExpiresDate] [int] NOT NULL,
    [fkUserGuid] [uniqueidentifier] NOT NULL,
    [fkDeviceGuid] [uniqueidentifier] NOT NULL
     */
    List<UserSession> findBySessionToken(UUID sessionUid);

    List<UserSession> findByFkDeviceGuid(UUID deviceUid);

    List<UserSession> findAllBySessionTokenExpiresDateAfterOrderBySessionTokenExpiresDate(int tsAfter);

    List<UserSession> findBySessionTokenExpiresDateAfterOrderBySessionTokenExpiresDate(int tsStart);

    List<UserSession> findByRememberUntilAfterOrderByRememberUntil(int tsStart);

}
