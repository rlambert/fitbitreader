package com.proclub.datareader.repositories;

import com.proclub.datareader.dao.DataCenterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DataCenterConfigRepo extends JpaRepository<DataCenterConfig, UUID> {
    /*
        fkUserGuid uniqueidentifier NOT NULL,
    SourceSystem int NOT NULL,
    LastChecked datetime NOT NULL,
    PanelDisplay int NOT NULL,
    Status int NOT NULL,
    StatusText varchar(150) NULL,
    DataStatus int NOT NULL,
    Credentials varchar(700) NULL,
    Modified datetime NOT NULL
     */
    List<DataCenterConfig> findAllByLastCheckedAfter(LocalDateTime dtStart);
    List<DataCenterConfig> findAllByLastCheckedBetween(LocalDateTime dtStart, LocalDateTime dtEnd);
    List<DataCenterConfig> findAllByModifiedAfter(LocalDateTime dtStart);
    List<DataCenterConfig> findAllByModifiedBetween(LocalDateTime dtStart, LocalDateTime dtEnd);
    List<DataCenterConfig> findAllByStatusAndSourceSystem(int status, int sourceSystem);

    List<DataCenterConfig> findAllByFkUserGuid(String userId);
    List<DataCenterConfig> findAllByFkUserGuidAndSourceSystem(String userId, int sourceSystem);

}
