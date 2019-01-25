package com.proclub.datareader.api.rest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping(value="/error")
public class CustomErrorController implements ErrorController {

    @RequestMapping(value = {"", "/"}, produces = "application/json")
    public ApiError error(HttpServletRequest req, HttpServletResponse resp) {

        Object objStatus = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = null;
        String msg = "Unknown Error";
        String error = "";

        if (objStatus != null) {
            Integer statusCode = Integer.valueOf(objStatus.toString());
            status  = resolve(statusCode);
            switch (status) {
                case BAD_REQUEST: {
                    msg = "Incorrect or malformed request.";
                    error = "Bad Request";
                    break;
                }
                case UNAUTHORIZED: {
                    msg = "Incorrect or invalid security credentials";
                    error = "Unauthorized";
                    break;
                }
                case FORBIDDEN: {
                    msg = "No access allowed";
                    error = "Forbidden";
                    break;
                }
                case NOT_FOUND: {
                    msg = "Resource not found";
                    error = "Not Found";
                    break;
                }
                case METHOD_NOT_ALLOWED: {
                    msg = "HTTP Method not allowed";
                    error = "Method Not Allowed";
                    break;
                }
                case NOT_ACCEPTABLE: {
                    msg = "Not acceptable";
                    error = "Not Acceptable";
                    break;
                }
                case REQUEST_TIMEOUT:{
                    msg = "Request timed out";
                    error = "Request Timeout";
                    break;
                }
                case CONFLICT: {
                    msg = "Resource conflict (may be locked)";
                    error = "Conflict";
                    break;
                }
                case INTERNAL_SERVER_ERROR:{
                    msg = "Internal server error (you probably found a bug)";
                    error = "Internal Server Error";
                    break;
                }
                case NOT_IMPLEMENTED:
                    break;
                case BAD_GATEWAY:{
                    msg = "Bad gateway";
                    error = "Bad Gateway";
                    break;
                }
                default: {
                    msg = status.getReasonPhrase();
                    error = status.toString();
                }
            }
        }
        return new ApiError(status, msg, error);
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}