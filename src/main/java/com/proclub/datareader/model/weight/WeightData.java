
package com.proclub.datareader.model.weight;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proclub.datareader.model.FitBitApiData;
import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class WeightData implements FitBitApiData {
    @JsonProperty("weight")
    private List<Weight> weight;

}
