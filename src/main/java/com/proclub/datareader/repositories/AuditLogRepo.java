package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {
    /**
     [Id]      	int IDENTITY(1,1) NOT NULL,
     [DateTime]	datetime NOT NULL,
     [UserId]  	varchar(40) NOT NULL,
     [Actvity] 	varchar(40) NOT NULL,
     [Details] 	varchar(4096) NULL
     */

    List<AuditLog> findByFkUserGuid(String uid);
    List<AuditLog> findByFkUserGuidAndDateTimeAfter(String uid, LocalDateTime dtStart);
    List<AuditLog> findByFkUserGuidAndDateTimeAfterAndActivityOrderByDateTimeDesc(String uid, LocalDateTime dtStart, String activity);
    List<AuditLog> findByFkUserGuidAndDateTimeBetweenAndActivityOrderByDateTimeDesc(String uid, LocalDateTime dtStart, LocalDateTime dtEnd, String activity);
    List<AuditLog> findByDateTimeAfter(LocalDateTime dtStart);
    List<AuditLog> findByDateTimeBetween(LocalDateTime dtStart, LocalDateTime dtEnd);
    List<AuditLog> findByActivityAndDateTimeAfter(String activity, LocalDateTime dtStart);
    List<AuditLog> findByActivityAndDateTimeBetween(String activity, LocalDateTime dtStart, LocalDateTime dtEnd);
    List<AuditLog> findByFkUserGuidAndActivityAndDateTimeBetween(String uid, String activity, LocalDateTime dtStart, LocalDateTime dtEnd);
    List<AuditLog> findByFkUserGuidAndDateTimeBetween(String uid, LocalDateTime dtStart, LocalDateTime dtEnd);

}
