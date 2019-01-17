
package com.proclub.datareader.model.activitylevel;

import lombok.Data;

import java.util.List;

@Data
@SuppressWarnings("unused")
public class Summary {

    private long activityCalories;
    private long caloriesBMR;
    private long caloriesOut;
    private List<Distance> distances;
    private double elevation;
    private long fairlyActiveMinutes;
    private long floors;
    private long lightlyActiveMinutes;
    private long marginalCalories;
    private long sedentaryMinutes;
    private long steps;
    private long veryActiveMinutes;

}
