/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author vertigo
 */
public class DateUtil {

    /**
     * @param nbMinutes
     * @return a String that contains a timestamp + the nb of minutes sent as a
     * parameter. It can also be used to substract some minutes and use it for
     * filtering data based on timestamps that are indexed.
     */
    public static String getMySQLTimestampTodayDeltaMinutes(int nbMinutes) {
        // Calculating today + n minutes.
        // 
        Date today = new Date(); // Getting now.
        SimpleDateFormat formater; // Define the MySQL Format.
        formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.MINUTE, nbMinutes);
        return formater.format(cal.getTime());
    }

    public static String getTodayFormat(String format) {
        // Calculating today + n minutes.
        // 
        Date today = new Date(); // Getting now.
        SimpleDateFormat formater; // Define the MySQL Format.
        formater = new SimpleDateFormat(format);

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        return formater.format(cal.getTime());
    }

    public static String getYesterdayFormat(String format) {
        // Calculating today + n minutes.
        // 
        Date today = new Date(); // Getting now.
        SimpleDateFormat formater; // Define the MySQL Format.
        formater = new SimpleDateFormat(format);

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.HOUR, -24);
        return formater.format(cal.getTime());
    }

    public static String getFormatedDate(Long l) {
        String myDate;
        myDate = String.valueOf(l).substring(10, 12) + ":" + String.valueOf(l).substring(12, 14) + "." + String.valueOf(l).substring(14, 15);
        return myDate;
    }

    /**
     * This is still to be implemented and should return a string with formated
     * time elapsed between the 2 timestamps.
     *
     * @param startL
     * @param endL
     * @return
     */
    public static String getFormatedElapsed(Long startL, Long endL) {
        try {
            String dateStart = String.valueOf(startL);
            String dateEnd = String.valueOf(endL);
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            Date dS = df.parse(dateStart);
            Date dE = df.parse(dateEnd);
            double elap = ((double) (dE.getTime() - dS.getTime()));

            if (elap >= 0) {
                if (elap > 60000) {
                    int elapm = (int) elap / 60000; // Get the Interger number of minutes.
                    double elapmin = elap - (elapm * 60000); // Get the additional millisecond removing the integer number of minutes.
                    String res = "<span class=\"verylong\">" + String.valueOf(elapm) + ":" + String.valueOf((int) elapmin / 1000) + "</span>";
                    return res;
                } else if (elap > 1000) {
                    int elaps = (int) elap / 100;
                    String res = String.valueOf((double) (elaps) / 10) + " s";
                    if ((elap) > 10000) {
                        res = "<span class=\"long\">" + res + "</span>";
                    }
                    return res;
                } else {
                    return String.valueOf((int) elap) + " ms";
                }
            } else {
                return "NA";
            }
        } catch (ParseException ex) {
            return "NA";
        }
    }
}
