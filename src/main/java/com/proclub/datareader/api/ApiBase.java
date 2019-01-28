/*
 -----------------------------------------
   ApiBase
   Copyright (c) 2018
   Blueprint Technologies
   All Right Reserved
 -----------------------------------------
 */

package com.proclub.datareader.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proclub.datareader.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ApiBase {

    /** Logger for this class. */
    private static final Logger _logger = LoggerFactory.getLogger(ApiBase.class.getName());

    // a Jackson mapper for serialization/deserialization
    private ObjectMapper _mapper = new ObjectMapper();

    protected ObjectMapper getMapper() {
        return _mapper;
    }

    // a helper method for serialization
    protected String serialize(Object obj) throws JsonProcessingException {
        return _mapper.writeValueAsString(obj);
    }

    // a helper method for deserializtion
    protected <T extends Object> T deserialize(String json, Class<T> theClass) throws IOException {
        return theClass.cast(_mapper.readValue(json, theClass));
    }

    protected String generateJsonView(HttpServletRequest req, String json) throws IOException {
        String html = StringUtils.readResource(this, "static/jsonview.html");
        return html.replace("{json}", json).replace("{apiUrl}", req.getRequestURL());
    }

    /**
     * all of our JSON-emitting APIs need to support an OPTIONS call for CORS
     * @param response - HttpServletResponse
     * @return null
     */
    @RequestMapping(value={"*"}, method= RequestMethod.OPTIONS)
    public View getOptions(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
        return null;
    }
}
