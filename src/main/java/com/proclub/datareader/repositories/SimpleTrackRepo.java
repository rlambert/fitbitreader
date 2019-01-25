package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.SimpleTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public interface SimpleTrackRepo extends JpaRepository<SimpleTrack, UUID> {
    /*
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
      */

    List<SimpleTrack> findByFkUserGuid(UUID uid);

    List<SimpleTrack> findByFkUserGuidAndTrackDateTimeAfter(UUID uid, LocalDateTime dtStart);

    List<SimpleTrack> findByFkUserGuidAndTrackDateTimeBetween(UUID uid, LocalDateTime dtStart, LocalDateTime dtEnd);

    List<SimpleTrack> findAllByTrackDateTimeBetween(LocalDateTime dtStart, LocalDateTime dtEnd);

    List<SimpleTrack> findAllByModifiedDateTimeBetween(LocalDateTime dtStart, LocalDateTime dtEnd);

    List<SimpleTrack> findAllByEntityTypeAndTrackDateTimeBetween(int entityType, LocalDateTime dtStart, LocalDateTime dtEnd);

    List<SimpleTrack> findByTrackDateTimeBetweenAndFkUserGuidAndSourceSystemAndEntityTypeOrderByTrackDateTime(
                        LocalDateTime dtStart, LocalDateTime dstEnd, UUID userId, int sourceSystem, int entityType);

}
