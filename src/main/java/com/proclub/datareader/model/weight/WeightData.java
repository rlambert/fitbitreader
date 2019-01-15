
package com.proclub.datareader.model.weight;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class WeightData {
    @JsonProperty("weight")
    private List<Weight> weight;

}
