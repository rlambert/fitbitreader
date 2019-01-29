/*
 -----------------------------------------
   StringUtils
   Copyright (c) 2015
   Digital Provisioners
   All Right Reserved (used with permission)
 -----------------------------------------
 */

package com.proclub.datareader.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StringUtils {

    //---------------------------------- static variables

    private static final Logger _logger	= LoggerFactory.getLogger(StringUtils.class.getName());


    //---------------------------------- methods

    // helper methods to read a resource
    private static String streamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * helper method to read a text resource
     * @param parent - Object, the parent object that owns the resource
     * @param resourceName - String, name of the resource file
     * @return String, content of resource
     * @throws IOException - when the resource cannot be read
     */
    public static String readResource(Object parent, String resourceName) throws IOException {
        InputStream is = parent.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException(String.format("Could not read %s", resourceName));
        }
        String txt;
        try {
            txt = streamToString(is);
        }
        finally {
            is.close();
        }
        return txt;
    }

    /**
     * shorthand method for checking if a string is null
     * or empty
     * @param str - String
     * @return boolean
     */
    public static boolean isNullOrEmpty(String str)
    {
        return ((str == null) || (str.length() == 0));
    }

    /**
     * formats an error log message such that the name of the current class and
     * method prefixes the exception message and call stack
     * @param ex - Exception
     * @return - String
     */
    public static String formatError(Exception ex) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String[] pkg = stackTrace[2].getClassName().split("\\.");
        return pkg[pkg.length-1] + "." + stackTrace[2].getMethodName() + ": " + ex.getMessage() + "\r\n" + getStackTrace(ex);
    }

    public static String formatError(String msg, Exception ex) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String[] pkg = stackTrace[2].getClassName().split("\\.");
        return pkg[pkg.length-1] + "." + stackTrace[2].getMethodName() + ": " + msg + " - " + ex.getMessage() + "\r\n" + getStackTrace(ex);
    }
    /**
     * getStrackTrace returns a string version of a stack trace,
     * which is handy for logging.
     * @param ex - Throwable
     * @return - String
     */
    public static String getStackTrace(Throwable ex)
    {
        final int MAX_STACK             = 2048;
        final Writer result 			= new StringWriter();
        final PrintWriter printWriter 	= new PrintWriter(result);
        ex.printStackTrace(printWriter);
        String trace = result.toString();
        return (trace.length() > MAX_STACK) ? trace.substring(0, MAX_STACK) : trace;
    }

    /**
     * formats a log message such that the name of the current class and
     * method prefixes the message
     * @param msg - String
     * @return String
     */
    public static String formatMessage(String msg) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String[] pkg = stackTrace[2].getClassName().split("\\.");
        return pkg[pkg.length-1] + "." + stackTrace[2].getMethodName() + ": " + msg;
    }

    /**
     * getMap turns a delimited string of key-value pairs (like a querystring)
     * into a HashMap (e.g. "name=Ross&type=developer&title=Mr")
     * @param src	- String
     * @param itemDelim	- String
     * @param nvDelim	- String
     * @return Map<String, String>
     */
    public static Map<String, String> getMap(String src, String itemDelim, String nvDelim)
    {
        Map<String, String> map = new HashMap<String, String>();
        if (src != null)
        {
            String[] coll = src.split(itemDelim);

            if (_logger.isDebugEnabled())
            {
                for (String sp : coll)
                {
                    _logger.debug("GeneralUtils.getMap: item - {}", sp);
                }
            }

            for(String nvp : coll)
            {
                String[] kv = nvp.split(nvDelim);
                if (kv.length > 1)
                { map.put(kv[0], kv[1]); }
                else
                { map.put(kv[0], ""); }
            }
        }
        return map;
    }

}
