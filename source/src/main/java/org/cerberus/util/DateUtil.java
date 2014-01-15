/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author vertigo
 */
public class DateUtil {

    /**
     * SimpleDateFormat use to display date in Cerberus GUI
     */
    public static final String DATE_FORMAT_DISPLAY = "yyyy-MM-dd HH:mm:ss";

    
    /**
     * SimpleDateFormat use to parse SQL Date in Cerberus BO
     */
    public static final String DATE_FORMAT_TIMESTAMP = "yyyyMMddHHmmssSSS";

    
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
        formater = new SimpleDateFormat(DateUtil.DATE_FORMAT_DISPLAY);

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
        myDate = String.valueOf(l).substring(8, 10) + ":" + String.valueOf(l).substring(10, 12) + ":" + String.valueOf(l).substring(12, 14) + "." + String.valueOf(l).substring(14, 15);
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
            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_TIMESTAMP);
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
