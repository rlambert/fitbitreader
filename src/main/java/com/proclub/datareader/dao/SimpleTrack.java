package com.proclub.datareader.dao;

import com.proclub.datareader.model.sleep.Sleep;
import com.proclub.datareader.model.sleep.SleepData;
import com.proclub.datareader.model.steps.Steps;
import com.proclub.datareader.model.steps.StepsData;
import com.proclub.datareader.model.weight.Weight;
import com.proclub.datareader.model.weight.WeightData;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Data
public class SimpleTrack {

    public enum PartnerStatus
    {
        /*
         * This is the data in the SimpleTrack.Status column
         *
         * relationship between the user and the partner
         * the state will change as they sign-in, experience data fetch issues or opt-out of the partner
         */

        Null (-1),
        SignUp (0),     // new user for this partner
        Active (1),     // have successfully authed
        AuthErr (2),    // auth was lost, user must re-auth
        RefreshErr (3), // partner did not respond or threw error during transmission
        DataErr(4),     // partner sent invalid tracking data (failed LSO validation)
        OptOut (5);     // user has opted out of further automatic refreshes but can still view past data

        public short status;

        private PartnerStatus(int status) {
            this.status = (short) status;
        }
    }

    public enum DataStatus
    {
        /*
         * This is the SimpleTrack.DataStatus column
         *
         * used to control any running processes for a user for the same partner
         * ensures no two web pages or processes are running on the same user at the same time
         */

        Null(-1),
        New(0),         // ready for next data operation
        Update(1),      // in the middle of the auth process
        Refresh(2);      // in the middle of the data refresh process

        public short status;

        private DataStatus(int status) {
            this.status = (short) status;
        }

    }

    public enum SyncStatus
    {
        Null(-1),
        New(0),
        Updated(1),
        Deleted(2),
        DeletedInSync(3),
        InSync(4);
        
        public short syncStatus;
        
        private SyncStatus(int status) {
            this.syncStatus = (short) status;
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
        NULL((short) -1),
        WEIGHT((short) 0),
        STEPS((short) 1),
        SLEEP((short) 2),
        MOOD((short) 3),
        WATER((short) 4),
        SUPLEMENTS((short) 5);

        public short entityValue;

        Entity (short eval) {
            this.entityValue = eval;
        }
    }

    public enum SourceSystem
    {
        NULL((short) -1),
        WEBTRACKER((short) 0),
        IPHONE((short) 1),
        WINPHONE((short) 2),
        HEALTHVAULT((short) 3),
        GADGET((short) 4),
        PROCENTRAL((short) 5),
        ADMIN((short) 6),
        OTHER((short) 7),
        FITBIT((short) 8),
        NETPULSE((short) 9),
        GARMIN((short) 10),
        NIKE((short) 11);

        public short sourceSystem;
        private SourceSystem(short src) {
            this.sourceSystem = src;
        }
    }

    @Id
    @Column(name = "SimpleTrackGuid")
    private UUID simpleTrackGuid = NullValue.Guid;

    @Column(name = "fkUserGuid")
    private UUID fkUserGuid = NullValue.Guid;

    @Column(name = "fkProviderId")
    private int fkProviderId = NullValue.Int;

    @Column(name = "SourceSystem")
    private int sourceSystem = SourceSystem.NULL.sourceSystem;

    @Column(name = "ModifiedDateTime")
    private int modifiedDateTime = (int) NullValue.DateTimeUtc.toEpochSecond();

    @Column(name = "TrackDateTime")
    private int trackDateTime = (int) NullValue.DateTimeUtc.toEpochSecond();

    @Column(name = "EntityType")
    private short entityType = Entity.NULL.entityValue;

    @Column(name = "ValTinyInt")
    private short valTinyInt = NullValue.Byte; // default

    @Column(name = "ValInt")
    private int valInt = NullValue.Int;    // default

    @Column(name = "ValDec")
    private double valDec = NullValue.Decimal;

    @Column(name = "ValStr")
    private String valStr = NullValue.String;

    @Column(name = "Sync")
    private short sync = SyncStatus.Null.syncStatus;

    @Column(name = "ValInt2")
    private int valInt2;

    @Column(name = "ValTime")
    private int valTime;

    @Column(name = "ValTime2")
    private int valTime2;

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
    public SimpleTrack(UUID simpleTrackGuid, UUID fkUserGuid, int fkProviderId, int sourceSystem,
                       Instant modifiedDateTime, Instant trackDateTime, short entityType, short valTinyInt,
                       short valInt, double valDec, String valStr, short sync, short valInt2, int valTime,
                       int valTime2, byte deviceReported) {
        this.simpleTrackGuid = simpleTrackGuid;
        this.fkUserGuid = fkUserGuid;
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
     * @param sleepData - SleepData
     */
    public SimpleTrack (SleepData sleepData) {
        this.entityType = Entity.SLEEP.entityValue;
        this.simpleTrackGuid = UUID.randomUUID();
        this.modifiedDateTime = (int) Instant.now().toEpochMilli()/1000;

        if (sleepData.getSleep().size() > 0) {
            Sleep sleep = sleepData.getSleep().get(0);
            this.valInt = (int) sleep.getMinutesToFallAsleep();
            this.valDec = (int) (sleep.getMinutesAsleep()/60);

            // ProClub keeps time in seconds since Epoch
            this.valTime = (int) (Instant.parse(sleep.getStartTime()).toEpochMilli()/1000);
            this.valTime2 = (int) (Instant.parse(sleep.getEndTime()).toEpochMilli()/1000);
        }
        this.valInt2 = (int) sleepData.getSummary().getWake().getCount();
    }

    /**
     * ctor for Steps data
     * @param stepsData - StepsData
     */
    public SimpleTrack (StepsData stepsData) {
        this.entityType = Entity.STEPS.entityValue;
        this.simpleTrackGuid = UUID.randomUUID();
        this.modifiedDateTime = (int) Instant.now().toEpochMilli()/1000;

        // we are assuming one day of data!
        if (stepsData.getSteps().size() > 0) {
            Steps steps = stepsData.getSteps().get(0);
            String dtStr = steps.getDateTime();   // YYYY-MM-DD
            //         String dtStr = "2018-12-11T00:00:00.00Z";
            if (!dtStr.contains(":")) {
                dtStr += "T00:00:00.00Z";
            }
            Instant dtTrack = Instant.parse(dtStr);
            this.trackDateTime = (int) dtTrack.toEpochMilli()/1000;
            this.valInt = (int) steps.getValue();
        }
        else {
            throw new IllegalArgumentException("No step data present in stepsData.");
        }
    }

    /**
     * constructor for weight data
     * @param wtdata - WeightData
     */
    public SimpleTrack(WeightData wtdata) {
        this.entityType = Entity.WEIGHT.entityValue;
        this.simpleTrackGuid = UUID.randomUUID();
        this.modifiedDateTime = (int) Instant.now().toEpochMilli()/1000;
        if (wtdata.getWeight().size() == 0) {
            throw new IllegalArgumentException("There is no weight data in the result.");
        }
        Weight wt = wtdata.getWeight().get(0);
        this.valDec = wt.getWeight();
        this.trackDateTime = (int) (Instant.parse(wt.getDate() + "T" + wt.getTime() + ".00Z").toEpochMilli()/1000);
    }


}
