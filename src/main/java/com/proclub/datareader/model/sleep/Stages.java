
package com.proclub.datareader.model.sleep;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Stages {

    private long deep;
    private long light;
    private long rem;
    private long wake;

}
