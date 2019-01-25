
package com.proclub.datareader.model.sleep;

import java.util.List;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Levels {

    private List<Datum> data;
    private List<ShortDatum> shortData;
    private Summary summary;

}
