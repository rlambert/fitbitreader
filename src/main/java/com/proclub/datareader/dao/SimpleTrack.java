package com.proclub.datareader.dao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class SimpleTrack {

    @Id
    @Column(name = "SimpleTrackGuid")
    private UUID simpleTrackGuid;

    @Column(name = "fkUserGuid")
    private UUID fkUserGuid;

    @Column(name = "fkProviderId")
    private int fkProviderId;

    @Column(name = "SourceSystem")
    private int sourceSystem;

    @Column(name = "ModifiedDateTime")
    private Instant modifiedDateTime;

    @Column(name = "TrackDateTime")
    private Instant trackDateTime;

    @Column(name = "EntityType")
    private int entityType;

    @Column(name = "ValTinyInt")
    private short valTinyInt;

    @Column(name = "ValInt")
    private short valInt;

    @Column(name = "ValDec")
    private double valDec;

    @Column(name = "ValStr")
    private double valStr;

    @Column(name = "Sync")
    private double sync;

    @Column(name = "ValInt2")
    private short valInt2;

    @Column(name = "ValTime")
    private int valTime;

    @Column(name = "ValTime2")
    private int valTime2;

    @Column(name = "DeviceReported")
    private byte deviceReported;

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

    public SimpleTrack() {
    }

    /**
     * full-arg constructor
     * @param simpleTrackGuid
     * @param fkUserGuid
     * @param fkProviderId
     * @param sourceSystem
     * @param modifiedDateTime
     * @param trackDateTime
     * @param entityType
     * @param valTinyInt
     * @param valInt
     * @param valDec
     * @param valStr
     * @param sync
     * @param valInt2
     * @param valTime
     * @param valTime2
     * @param deviceReported
     */
    public SimpleTrack(UUID simpleTrackGuid, UUID fkUserGuid, int fkProviderId, int sourceSystem,
                       Instant modifiedDateTime, Instant trackDateTime, int entityType, short valTinyInt,
                       short valInt, double valDec, double valStr, double sync, short valInt2, int valTime,
                       int valTime2, byte deviceReported) {
        this.simpleTrackGuid = simpleTrackGuid;
        this.fkUserGuid = fkUserGuid;
        this.fkProviderId = fkProviderId;
        this.sourceSystem = sourceSystem;
        this.modifiedDateTime = modifiedDateTime;
        this.trackDateTime = trackDateTime;
        this.entityType = entityType;
        this.valTinyInt = valTinyInt;
        this.valInt = valInt;
        this.valDec = valDec;
        this.valStr = valStr;
        this.sync = sync;
        this.valInt2 = valInt2;
        this.valTime = valTime;
        this.valTime2 = valTime2;
        this.deviceReported = deviceReported;
    }
}
