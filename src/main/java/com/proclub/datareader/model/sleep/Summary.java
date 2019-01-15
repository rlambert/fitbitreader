
package com.proclub.datareader.model.sleep;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Summary {

    private Deep deep;
    private Light light;
    private Rem rem;
    private Stages stages;
    private long totalMinutesAsleep;
    private long totalSleepRecords;
    private long totalTimeInBed;
    private Wake wake;

}
