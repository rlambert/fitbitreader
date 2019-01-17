package com.proclub.datareader.model.activitylevel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.utils.StringUtils;
import org.junit.Test;

import java.io.IOException;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

public class ActivityLevelJsonTests {

    @Test
    public void testActivityLevelResults() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = StringUtils.readResource(this, "activityleveldata.json");
        ActivityLevelData act = mapper.readValue(json, ActivityLevelData.class);
        assertNotNull(act);
    }

}
