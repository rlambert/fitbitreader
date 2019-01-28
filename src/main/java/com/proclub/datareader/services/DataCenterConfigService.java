package com.proclub.datareader.services;

import com.proclub.datareader.dao.DataCenterConfig;
import com.proclub.datareader.dao.DataCenterConfigRowMapper;
import com.proclub.datareader.dao.SimpleTrack;
import com.proclub.datareader.repositories.DataCenterConfigRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private static String _select1Sql = "SELECT fkUserGuid, SourceSystem, LastChecked, PanelDisplay, Status, " +
                    "StatusText, DataStatus, Credentials, Modified FROM dbo.DataCenterConfig WHERE fkUserGuid = %s AND " +
                    "SourceSystem = %s";

    private static String _insertSql = "INSERT INTO dbo.DataCenterConfig (fkUserGuid, SourceSystem, LastChecked, PanelDisplay, " +
                    "Status, StatusText, DataStatus, Credentials, Modified) VALUES ('%s', %s, '%s', %s, %s, '%s', " +
                    "%s, '%s', '%s')";

    private static String _updateSql = "UPDATE dbo.DataCenterConfig SET LastChecked='%s', PanelDisplay=%s, Status=%s, " +
                    "StatusText='%s', DataStatus=%s, Credentials='%s', Modified='%s' WHERE fkUserGuid='%s' AND SourceSystem=%s";


    private DataCenterConfigRepo _repo;
    private JdbcTemplate _jdbc;
    private DataCenterConfigRowMapper _mapper = new DataCenterConfigRowMapper();

    /**
     * ctor
     *
     * @param repo - ActivityLevelRepo
     */
    @Autowired
    public DataCenterConfigService(DataCenterConfigRepo repo, JdbcTemplate template) {
        _repo = repo;
        _jdbc = template;
    }

    public DataCenterConfig createDataCenterConfig(DataCenterConfig dc) {
        //return _repo.save(dc);
        /* fkUserGuid, SourceSystem, LastChecked, PanelDisplay, Status, StatusText, DataStatus, Credentials, Modified
         */
        String sql = String.format(_insertSql, dc.getFkUserGuid(), dc.getSourceSystem(), dc.getLastChecked(),
                        dc.getPanelDisplay(), dc.getStatus(), dc.getStatusText(), dc.getDataStatus(), dc.getCredentials(), dc.getModified());
        _jdbc.update(sql);
        return dc;
    }

    public DataCenterConfig updateDataCenterConfig(DataCenterConfig dc) {
        // UPDATE dbo.DataCenterConfig SET LastChecked='%s', PanelDisplay=%s, Status=%s, " +
        //                    "StatusText='%s', DataStatus=%s, Credentials='%s', Modified='%s' WHERE fkUserGuid='%s' AND SourceSystem=%s
        dc.setModified(LocalDateTime.now());
        String sql = String.format(_updateSql, dc.getLastChecked(), dc.getPanelDisplay(), dc.getStatus(),
                                    dc.getStatusText(), dc.getDataStatus(), dc.getCredentials(), dc.getModified(), dc.getFkUserGuid(), dc.getSourceSystem());
        _jdbc.update(sql);
        return dc;
    }

    public void deleteDataCenterConfig(DataCenterConfig dc) {
        _repo.delete(dc);
    }


    public Optional<DataCenterConfig> findById(String id, int sourceSystem) {
        List<DataCenterConfig> rows = _repo.findAllByFkUserGuidAndSourceSystem(id, sourceSystem);
        if (rows.size() > 0) {
            return Optional.of(rows.get(0));
        }
        else {
            return Optional.empty();
        }
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
        List<DataCenterConfig> rows = _repo.findAllByStatusAndSourceSystem(status, sourceSystem);
        return rows;
    }

}
