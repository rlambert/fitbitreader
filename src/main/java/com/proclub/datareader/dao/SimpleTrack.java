package com.proclub.datareader.dao;

import com.proclub.datareader.model.sleep.Sleep;
import com.proclub.datareader.model.steps.Steps;
import com.proclub.datareader.model.weight.Weight;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name="SimpleTrack",schema="dbo")
@Data
public class SimpleTrack {

    public enum SyncStatus
    {
        Null(-1),
        New(0),
        Updated(1),
        Deleted(2),
        DeletedInSync(3),
        InSync(4);
        
        public int syncStatus;
        
        private SyncStatus(int status) {
            this.syncStatus = (int) status;
        }
    }
    
    /**
     * There are the default values for this class by type as defined by
     * original application
     */
    public static class NullValue {

        static final ZonedDateTime DateTimeUtc = LocalDateTime.of(1900, 9, 9, 9, 9, 9).atZone(ZoneId.of("UTC"));
        static final int Int = -1;
        static final byte Byte = (byte) 255;
        static final UUID Guid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        static final String String = "";
        static final double Decimal = -1.0000;
        static final int Enum = -1;
    }

    public enum TrackList
    {
        Null(-1),
        FoodBreak(0),
        FoodLunch(1),
        FoodDinner(2),
        FoodSnack(3),
        HungerBreak(4),
        HungerLunch(5),
        HungerDinner(6),
        HungerSnack(7),
        PlateModel(8),
        Cardio(9),
        Strength(10),
        Weight(11),
        Steps(12),
        Sleep(13),
        Mood(14),
        Water(15),
        Supplement(16),
        FoodMealTimes(17),
        Workout2020(18);

        private int trackItem;
        private TrackList(int item) {
            this.trackItem = item;
        }
    }
    
    public enum Entity {
        NULL((int) -1),
        WEIGHT((int) 0),
        STEPS((int) 1),
        SLEEP((int) 2),
        MOOD((int) 3),
        WATER((int) 4),
        SUPLEMENTS((int) 5);

        public int entityValue;

        Entity (int eval) {
            this.entityValue = eval;
        }
    }

    public enum SourceSystem
    {
        NULL((int) -1),
        WEBTRACKER((int) 0),
        IPHONE((int) 1),
        WINPHONE((int) 2),
        HEALTHVAULT((int) 3),
        GADGET((int) 4),
        PROCENTRAL((int) 5),
        ADMIN((int) 6),
        OTHER((int) 7),
        FITBIT((int) 8),
        NETPULSE((int) 9),
        GARMIN((int) 10),
        NIKE((int) 11);

        public int sourceSystem;
        private SourceSystem(int src) {
            this.sourceSystem = src;
        }
    }

    @Id
    @Column(name = "SimpleTrackGuid")
    private String simpleTrackGuid = UUID.randomUUID().toString().toUpperCase();
    public void setSimpleTrackGuid(String id) {
        this.simpleTrackGuid = id.toUpperCase();
    }

    @Column(name = "fkUserGuid")
    private String fkUserGuid = NullValue.Guid.toString();
    public void setFkUserGuid(String id) {
        this.fkUserGuid = id.toUpperCase();
    }

    @Column(name = "fkProviderId")
    private int fkProviderId = NullValue.Int;

    @Column(name = "SourceSystem")
    private int sourceSystem; // = SourceSystem.NULL.sourceSystem;

    @Column(name = "ModifiedDateTime")
    private int modifiedDateTime = (int) NullValue.DateTimeUtc.toEpochSecond();

    @Column(name = "TrackDateTime")
    private int trackDateTime = (int) NullValue.DateTimeUtc.toEpochSecond();

    @Column(name = "EntityType")
    private int entityType;  // = Entity.NULL.entityValue;

    @Column(name = "ValTinyInt")
    private int valTinyInt; // = NullValue.Byte; // default

    @Column(name = "ValInt")
    private int valInt = NullValue.Int;    // default

    @Column(name = "ValDec")
    private double valDec = NullValue.Decimal;

    @Column(name = "ValStr")
    private String valStr = NullValue.String;

    @Column(name = "Sync")
    private int sync; // = SyncStatus.Null.syncStatus;

    @Column(name = "ValInt2")
    private int valInt2;

    @Column(name = "ValTime")
    private int valTime = -1;

    @Column(name = "ValTime2")
    private int valTime2 = -1;

    @Column(name = "DeviceReported")
    private byte deviceReported;

    /*  TABLE STRUCTURE
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
    public SimpleTrack(String simpleTrackGuid, String fkUserGuid, int fkProviderId, int sourceSystem,
                            Instant modifiedDateTime, Instant trackDateTime, int entityType, int valTinyInt,
                            int valInt, double valDec, String valStr, int sync, int valInt2, int valTime,
                            int valTime2, byte deviceReported) {
        this.simpleTrackGuid = simpleTrackGuid;
        this.fkUserGuid = fkUserGuid.toUpperCase();
        this.fkProviderId = fkProviderId;
        this.sourceSystem = sourceSystem;
        this.modifiedDateTime = (int) modifiedDateTime.toEpochMilli()/1000;
        this.trackDateTime = (int) trackDateTime.toEpochMilli()/1000;
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

    public SimpleTrack(String fkUserGuid, int fkProviderId, int sourceSystem,
                       Instant modifiedDateTime, Instant trackDateTime, int entityType, int valTinyInt,
                       int valInt, double valDec, String valStr, int sync, int valInt2, int valTime,
                       int valTime2, byte deviceReported) {
        this.fkUserGuid = fkUserGuid.toUpperCase();
        this.fkProviderId = fkProviderId;
        this.sourceSystem = sourceSystem;
        this.modifiedDateTime = (int) modifiedDateTime.toEpochMilli()/1000;
        this.trackDateTime = (int) trackDateTime.toEpochMilli()/1000;
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

    /**
     * ctor for Sleep data
     * @param sleep - Sleep
     * @param fkUserGuid - String
     */
    public SimpleTrack(Sleep sleep, String fkUserGuid) {
        this.entityType = Entity.SLEEP.entityValue;
        this.simpleTrackGuid = UUID.randomUUID().toString().toUpperCase();
        this.fkUserGuid = fkUserGuid.toUpperCase();
        this.sourceSystem = SourceSystem.FITBIT.sourceSystem;
        this.modifiedDateTime = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));
        this.valInt = (int) sleep.getMinutesToFallAsleep();
        this.valDec = (int) (sleep.getMinutesAsleep()/60);

        // ProClub keeps time in seconds since Epoch
        this.valTime = (int) sleep.getStartTimeEpochSeconds();
        this.valTime2 = (int) sleep.getEndTimeEpochSeconds();
        this.valInt2 = (int) sleep.getLevels().getSummary().getWake().getCount();

        String dtStr = sleep.getDateOfSleep();   // YYYY-MM-DD
        //         String dtStr = "2018-12-11T00:00:00.00Z";
        if (!dtStr.contains(":")) {
            dtStr += "T00:00:00.000";
        }
        LocalDateTime dtTrack = LocalDateTime.parse(dtStr);
        this.trackDateTime = (int) dtTrack.toEpochSecond(ZoneOffset.ofHours(0));
    }

    /**
     * ctor for Steps data
     * @param steps - Steps
     * @param fkUserGuid - String
     */
    public SimpleTrack (Steps steps, String fkUserGuid) {
        this.entityType = Entity.STEPS.entityValue;
        this.simpleTrackGuid = UUID.randomUUID().toString().toUpperCase();
        this.modifiedDateTime = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));
        this.fkUserGuid = fkUserGuid.toUpperCase();
        this.sourceSystem = SourceSystem.FITBIT.sourceSystem;

        String dtStr = steps.getDateTime();   // YYYY-MM-DD
        //         String dtStr = "2018-12-11T00:00:00.00Z";
        if (!dtStr.contains(":")) {
            dtStr += "T00:00:00.000";
        }
        LocalDateTime dtTrack = LocalDateTime.parse(dtStr);
        this.trackDateTime = (int) dtTrack.toEpochSecond(ZoneOffset.ofHours(0));
        this.valInt = steps.getValue();
    }

    /**
     * constructor for weight data
     * @param wt - Weight
     * @param fkUserGuid - String
     */
    public SimpleTrack(Weight wt, String fkUserGuid) {
        this.entityType = Entity.WEIGHT.entityValue;
        this.simpleTrackGuid = UUID.randomUUID().toString().toUpperCase();
        this.modifiedDateTime = (int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(0));
        this.valDec = wt.getWeight();
        String dtStr = wt.getDate();   // YYYY-MM-DD
        //         String dtStr = "2018-12-11T00:00:00.00Z";
        if (!dtStr.contains(":")) {
            dtStr += "T00:00:00.000";
        }
        LocalDateTime dtTrack = LocalDateTime.parse(dtStr);
        this.trackDateTime = (int) dtTrack.toEpochSecond(ZoneOffset.ofHours(0));
        this.fkUserGuid = fkUserGuid.toUpperCase();
        this.sourceSystem = SourceSystem.FITBIT.sourceSystem;
    }

}
