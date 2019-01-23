package com.proclub.datareader.services;

import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.repositories.DataCenterConfigRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DataCenterConfigService {

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

    private DataCenterConfigRepo _repo;

    /**
     * ctor
     *
     * @param repo - ActivityLevelRepo
     */
    @Autowired
    public DataCenterConfigService(DataCenterConfigRepo repo) {
        _repo = repo;
    }

    public DataCenterConfig createDataCenterConfig(DataCenterConfig dc) {
        return _repo.save(dc);
    }

    public DataCenterConfig updateDataCenterConfig(DataCenterConfig dc) {
        return _repo.save(dc);
    }

    public void deleteDataCenterConfig(DataCenterConfig dc) {
        _repo.delete(dc);
    }

    public void deleteDataCenterConfigById(DataCenterConfig.Pkey id) { _repo.deleteById(id);}

    public Optional<DataCenterConfig> findById(DataCenterConfig.Pkey id) {
        return _repo.findById(id);
    }

    public long count() {
        return _repo.count();
    }

    public List<DataCenterConfig> findAll() {
        return _repo.findAll();
    }

    public List<DataCenterConfig> findAllByModifiedDateTimeBetween(LocalDateTime dtStart, LocalDateTime dtEnd) {
        return _repo.findAllByModifiedBetween(dtStart, dtEnd);
    }
    public List<DataCenterConfig> findAllByModifiedDateTimeAfter(LocalDateTime dtStart) {
        return _repo.findAllByModifiedAfter(dtStart);
    }

    public List<DataCenterConfig> findAllByLastCheckedBetween(LocalDateTime dtStart, LocalDateTime dtEnd) {
        return _repo.findAllByLastCheckedBetween(dtStart, dtEnd);
    }
    public List<DataCenterConfig> findAllByLastCheckedAfter(LocalDateTime dtStart) {
        return _repo.findAllByLastCheckedAfter(dtStart);
    }

    public List<DataCenterConfig> findAllSorted(Sort sort) {
        return _repo.findAll(sort);
    }

    public List<DataCenterConfig> findAllFitbitActive() {
        int status = DataCenterConfig.PartnerStatus.Active.status;
        int sourceSystem = SimpleTrack.SourceSystem.FITBIT.sourceSystem;
        return _repo.findAllByStatusAndSourceSystem(status, sourceSystem);
    }

}
