
package com.proclub.datareader.model.sleep;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proclub.datareader.model.FitBitApiData;
import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class SleepData implements FitBitApiData {

    @JsonProperty("sleep")
    private List<Sleep> sleep;

    @JsonProperty("summary")
    private Summary summary;

}
