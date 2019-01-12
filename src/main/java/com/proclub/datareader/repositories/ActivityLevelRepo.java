package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.ActivityLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


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

    List<ActivityLevel> findByFkUserGuid(UUID uid);

    List<ActivityLevel> findByFkUserGuidAndTrackDateTimeAfter(UUID uid, Instant dtStart);

    List<ActivityLevel> findByFkUserGuidAndTrackDateTimeBetween(UUID uid, Instant dtStart, Instant dtEnd);

    List<ActivityLevel> findAllByTrackDateTimeBetween(Instant dtStart, Instant dtEnd);

    List<ActivityLevel> findAllByModifiedDateTimeBetween(Instant dtStart, Instant dtEnd);
}
