/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import java.util.ArrayList;

/**
 * @author vertigo
 */
public interface IDatabaseVersioningService {

    /**
     * @param SQLString String that contains the SQL that will be executed
     *                  against the Cerberus database.
     * @return "OK" if the SQL executed correctly and a string with the error
     *         when not executed.
     */
    String exeSQL(String SQLString);

    /**
     * @return true if the database is up to date and false if the database
     *         needs to be upgraded.
     */
    boolean isDatabaseUptodate();

    /**
     * @return an array of string that contain all the SQL instructions that
     *         needs to be executed in order to build the Cerberus database.
     */
    ArrayList<String> getSQLScript();
}
