/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.dto;

/**
 * Class that creates a row with statistics (total values and percentage values)
 */
public class SummaryStatisticsBugTrackerDTO {

    private String testFirst;
    private String testCaseFirst;
    private String testLast;
    private String testCaseLast;
    private String bugId;
    private String bugIdURL;
    private long exeIdFirst;
    private long exeIdLast;
    private String exeIdLastStatus;
    private int nbExe;
    private boolean toClean;

    public SummaryStatisticsBugTrackerDTO() {
        this.testFirst = "";
        this.testCaseFirst = "";
        this.bugId = "";
        this.nbExe = 0;
        this.toClean = false;
    }

    public String getExeIdLastStatus() {
        return exeIdLastStatus;
    }

    public void setExeIdLastStatus(String exeIdLastStatus) {
        this.exeIdLastStatus = exeIdLastStatus;
    }

    public String getBugIdURL() {
        return bugIdURL;
    }

    public void setBugIdURL(String bugIdURL) {
        this.bugIdURL = bugIdURL;
    }

    public long getExeIdFirst() {
        return exeIdFirst;
    }

    public void setExeIdFirst(long exeIdFirst) {
        this.exeIdFirst = exeIdFirst;
    }

    public long getExeIdLast() {
        return exeIdLast;
    }

    public void setExeIdLast(long exeIdLast) {
        this.exeIdLast = exeIdLast;
    }

    public boolean isToClean() {
        return toClean;
    }

    public void setToClean(boolean toClean) {
        this.toClean = toClean;
    }

    public String getTestFirst() {
        return testFirst;
    }

    public void setTestFirst(String testFirst) {
        this.testFirst = testFirst;
    }

    public String getTestCaseFirst() {
        return testCaseFirst;
    }

    public void setTestCaseFirst(String testCaseFirst) {
        this.testCaseFirst = testCaseFirst;
    }

    public String getTestLast() {
        return testLast;
    }

    public void setTestLast(String testLast) {
        this.testLast = testLast;
    }

    public String getTestCaseLast() {
        return testCaseLast;
    }

    public void setTestCaseLast(String testCaseLast) {
        this.testCaseLast = testCaseLast;
    }

    public String getTestCase() {
        return testCaseFirst;
    }

    public void setTestCase(String testCase) {
        this.testCaseFirst = testCase;
    }

    public String getBugId() {
        return bugId;
    }

    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    public int getNbExe() {
        return nbExe;
    }

    public void setNbExe(int nbExe) {
        this.nbExe = nbExe;
    }

}
