package com.proclub.datareader.dao;

import com.proclub.datareader.model.activitylevel.ActivityLevelData;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name="ActivityLevel", schema="dbo")
//@Table(name="ActivityLevel")
public class ActivityLevel {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ActivityLevelID")
    private long activityLevelId;

    @Column(name = "fkUserGuid")
    private String fkUserGuid;
    public void setFkUserGuid(String uid) {
        fkUserGuid = uid.toUpperCase();
    }

    @Column(name = "ModifiedDateTime")
    private LocalDateTime modifiedDateTime;

    @Column(name = "TrackDateTime")
    private LocalDateTime trackDateTime;

    @Column(name = "FairlyActiveMinutes")
    private int fairlyActiveMinutes;

    @Column(name = "LightlyActiveMinutes")
    private int lightlyActiveMinutes;

    @Column(name = "VeryActiveMinutes")
    private int veryActiveMinutes;

    @Column(name = "DeviceReported")
    private boolean deviceReported;

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

    public ActivityLevel() {
    }

    /**
     * full arg constructor
     * @param fkUserGuid
     * @param modifiedDateTime
     * @param trackDateTime
     * @param fairlyActiveMinutes
     * @param lightlyActiveMinutes
     * @param veryActiveMinutes
     * @param deviceReported
     */
    public ActivityLevel(String fkUserGuid, LocalDateTime modifiedDateTime, LocalDateTime trackDateTime, int fairlyActiveMinutes,
                         int lightlyActiveMinutes, int veryActiveMinutes, boolean deviceReported) {
        this.fkUserGuid = fkUserGuid;
        this.modifiedDateTime = modifiedDateTime;
        this.trackDateTime = trackDateTime.truncatedTo(ChronoUnit.SECONDS);
        this.fairlyActiveMinutes = fairlyActiveMinutes;
        this.lightlyActiveMinutes = lightlyActiveMinutes;
        this.veryActiveMinutes = veryActiveMinutes;
        this.deviceReported = deviceReported;
    }

    /**
     * This constructor takes the raw ActivityLevelData response from FitBit
     * and extracts what ProClub wants to keep from it into this DTO class.
     * @param fkUserGuid - UUID
     * @param trackDateTime - Instant
     * @param actData - Instant
     */
    public ActivityLevel (String fkUserGuid, LocalDateTime trackDateTime, ActivityLevelData actData) {
        this.fkUserGuid = fkUserGuid;
        this.modifiedDateTime = LocalDateTime.now();
        this.trackDateTime = trackDateTime.truncatedTo(ChronoUnit.SECONDS);
        this.fairlyActiveMinutes = (int) actData.getSummary().getFairlyActiveMinutes();
        this.lightlyActiveMinutes = (int) actData.getSummary().getLightlyActiveMinutes();
        this.veryActiveMinutes = (int) actData.getSummary().getVeryActiveMinutes();
        this.deviceReported = true;
    }


}
