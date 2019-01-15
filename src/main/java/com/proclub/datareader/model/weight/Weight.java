
package com.proclub.datareader.model.weight;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Weight {

    private double bmi;
    private String date;
    private int fat;
    private long logId;
    private String source;
    private String time;
    private double weight;

}
