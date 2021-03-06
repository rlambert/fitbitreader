package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.ActivityLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface ActivityLevelRepo extends JpaRepository<ActivityLevel, Long> {
    /**
     * CREATE TABLE [dbo].[ActivityLevel](
     *     [ActivityLevelID] [bigint] IDENTITY(1,1) NOT NULL,
     *     [fkUserGuid] [uniqueidentifier] NOT NULL,
     *     [ModifiedDateTime] [datetime] NOT NULL,
     *     [TrackDateTime] [datetime] NOT NULL,
     *     [FairlyActiveMinutes] [int] NOT NULL,
     *     [LightlyActiveMinutes] [int] NOT NULL,
     *     [VeryActiveMinutes] [int] NOT NULL,
     *     [DeviceReported] [bit] NOT NULL,
     */
    List<ActivityLevel> findByActivityLevelId(long id);

    List<ActivityLevel> findByFkUserGuid(String uid);

    List<ActivityLevel> findByFkUserGuidAndTrackDateTimeAfter(String uid, LocalDateTime dtStart);

    List<ActivityLevel> findByFkUserGuidAndTrackDateTimeBetweenOrderByTrackDateTime(String uid, LocalDateTime dtStart, LocalDateTime dtEnd);

    List<ActivityLevel> findAllByTrackDateTimeBetween(LocalDateTime dtStart, LocalDateTime dtEnd);

    List<ActivityLevel> findAllByModifiedDateTimeBetween(LocalDateTime dtStart, LocalDateTime dtEnd);
}
