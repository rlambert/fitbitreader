package com.proclub.datareader.model.steps;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StepsData {
    @JsonProperty("activities-steps")
    private List<Steps> steps;
}
