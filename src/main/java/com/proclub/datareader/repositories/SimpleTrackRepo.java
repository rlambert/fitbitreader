package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.SimpleTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SimpleTrackRepo extends JpaRepository<SimpleTrack, String> {
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

    List<SimpleTrack> findByFkUserGuid(String uid);

    List<SimpleTrack> findByFkUserGuidAndEntityTypeAndSourceSystemAndTrackDateTimeBetweenOrderByTrackDateTime(
                        String uid, int entityType, int sourceSystem, int dtStart, int dtEnd);

    List<SimpleTrack> findAllByTrackDateTimeBetween(int dtStart, int dtEnd);

    List<SimpleTrack> findAllByModifiedDateTimeBetween(int dtStart, int dtEnd);

    List<SimpleTrack> findAllByEntityTypeAndTrackDateTimeBetween(int entityType, int dtStart, int dtEnd);

}
