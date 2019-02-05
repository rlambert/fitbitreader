package com.proclub.datareader.dao;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DataCenterConfigRowMapper implements RowMapper {

    private String fixDateTimeStr(String dtStr) {
        if (dtStr.contains(":")) {
            if (!dtStr.contains("T")) {
                dtStr = dtStr.replace(" ", "T");
            }
        }
        return dtStr;
    }

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

    public DataCenterConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
        DataCenterConfig dc = new DataCenterConfig();
        dc.setFkUserGuid(rs.getString("fkUserGuid"));
        dc.setSourceSystem(rs.getInt("SourceSystem"));
        LocalDateTime dt = LocalDateTime.parse(fixDateTimeStr(rs.getString("LastChecked")));
        dc.setLastChecked(dt);
        dc.setPanelDisplay(rs.getInt("PanelDisplay"));
        dc.setStatus(rs.getInt("Status"));
        dc.setStatusText(rs.getString("StatusText"));
        dc.setDataStatus(rs.getInt("DataStatus"));
        dc.setCredentials(rs.getString("Credentials"));
        dt = LocalDateTime.parse(fixDateTimeStr(rs.getString("Modified")));
        dc.setModified(dt);
        return dc;
    }
}