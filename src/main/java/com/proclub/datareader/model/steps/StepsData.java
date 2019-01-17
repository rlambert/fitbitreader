package com.proclub.datareader.model.steps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proclub.datareader.model.FitBitApiData;
import lombok.Data;

import java.util.List;

@Data
public class StepsData implements FitBitApiData {
    @JsonProperty("activities-steps")
    private List<Steps> steps;
}
