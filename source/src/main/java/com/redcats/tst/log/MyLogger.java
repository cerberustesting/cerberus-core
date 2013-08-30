package com.redcats.tst.log;

import org.apache.log4j.Level;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/02/2013
 * @since 2.0.0
 */
public class MyLogger {

    public static void log(String className, Level level, String message) {
        org.apache.log4j.Logger.getLogger(className).log(level, message);
    }
}
