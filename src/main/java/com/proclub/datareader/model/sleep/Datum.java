
package com.proclub.datareader.model.sleep;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Datum {

    private String dateTime;
    private String level;
    private long seconds;

}
