
package com.proclub.datareader.model.sleep;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Wake {

    private long count;
    private long minutes;
    private long thirtyDayAvgMinutes;

}
