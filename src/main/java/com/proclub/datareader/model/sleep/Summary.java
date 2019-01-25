
package com.proclub.datareader.model.sleep;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Summary {

    private Deep deep;
    private Light light;
    private Rem rem;
    private Wake wake;
    private Asleep asleep;
    private Awake awake;
    private Restless restless;
}
