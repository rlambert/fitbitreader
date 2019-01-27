
package com.proclub.datareader.model.sleep;

import com.proclub.datareader.model.FitBitApiData;
import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class SleepData implements FitBitApiData {

    private List<Sleep> sleep;

}
