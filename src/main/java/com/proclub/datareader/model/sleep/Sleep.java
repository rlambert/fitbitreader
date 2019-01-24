
package com.proclub.datareader.model.sleep;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@SuppressWarnings("unused")
public class Sleep {

    private String dateOfSleep;
    private long duration;
    private long efficiency;
    private String endTime;
    private long infoCode;
    private Boolean isMainSleep;
    private Levels levels;
    private long logId;
    private long minutesAfterWakeup;
    private long minutesAsleep;
    private long minutesAwake;
    private long minutesToFallAsleep;
    private String startTime;
    private long timeInBed;
    private String type;

    public LocalDateTime getStartTime() {
        return LocalDateTime.parse(this.startTime);
    }

    public LocalDateTime getEndTime() {
        return LocalDateTime.parse(this.endTime);
    }

    public long getStartTimeEpochSeconds() {
        LocalDateTime dt = LocalDateTime.parse(this.startTime);
        return dt.toEpochSecond(ZoneOffset.ofHours(0));
    }

    public long getEndTimeEpochSeconds() {
        LocalDateTime dt = LocalDateTime.parse(this.endTime);
        return dt.toEpochSecond(ZoneOffset.ofHours(0));
    }
}
