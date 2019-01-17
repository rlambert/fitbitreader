
package com.proclub.datareader.model.activitylevel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proclub.datareader.model.FitBitApiData;
import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class ActivityLevelData implements FitBitApiData {

    @JsonProperty("activities")
    private List<Activity> activities;
    private Goals goals;
    private Summary summary;

}
