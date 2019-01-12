package com.proclub.datareader.dao;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@IdClass(DataCenterConfig.Pkey.class)
@Table(name="DataCenterConfig")
@Data
public class DataCenterConfig {

    @Data
    public static class Pkey implements Serializable {
        private UUID fkUserGuid;
        private int sourceSystem;

        public Pkey() {}

        public Pkey(UUID fkUserGuid, int sourceSystem) {
            this.fkUserGuid = fkUserGuid;
            this.sourceSystem = sourceSystem;
        }
    }

    @Id
    private UUID fkUserGuid;

    @Id
    @Column(name="SourceSystem")
    private int sourceSystem;

    @Column(name="LastChecked")
    private Instant lastChecked;

    @Column(name="PanelDisplay")
    private int panelDisplay;

    @Column(name="Status")
    private int status;

    @Column(name="StatusText")
    private String statusText;

    @Column(name="DataStatus")
    private int dataStatus;

    @Column(name="Credentials")
    private String credentials;

    @Column(name="Modified")
    private Instant modified;

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

    public DataCenterConfig() {}

    /**
     *
     * @param fkUserGuid
     * @param sourceSystem
     * @param lastChecked
     * @param panelDisplay
     * @param status
     * @param statusText
     * @param dataStatus
     * @param credentials
     * @param modified
     */
    public DataCenterConfig(UUID fkUserGuid, int sourceSystem, Instant lastChecked, int panelDisplay, int status,
                            String statusText, int dataStatus, String credentials, Instant modified) {
        this.fkUserGuid = fkUserGuid;
        this.sourceSystem = sourceSystem;
        this.lastChecked = lastChecked;
        this.panelDisplay = panelDisplay;
        this.status = status;
        this.statusText = statusText;
        this.dataStatus = dataStatus;
        this.credentials = credentials;
        this.modified = modified;
    }
}
