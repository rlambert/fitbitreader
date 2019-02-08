
package com.proclub.datareader.model.activitylevel;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Activity {

    private long activityId;
    private long activityParentId;
    private String activityParentName;
    private long calories;
    private String description;
    private double distance;
    private long duration;
    private Boolean hasStartTime;
    private Boolean isFavorite;
    private long logId;
    private String name;
    private String startDate;
    private String startTime;
    private long steps;
    private String lastModified;
}
