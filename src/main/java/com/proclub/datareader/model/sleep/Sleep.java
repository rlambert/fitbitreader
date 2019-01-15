
package com.proclub.datareader.model.sleep;

import lombok.Data;

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

}
