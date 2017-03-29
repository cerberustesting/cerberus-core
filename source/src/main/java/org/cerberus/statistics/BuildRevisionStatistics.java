/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.statistics;

/**
 *
 * @author bcivel
 */
public class BuildRevisionStatistics {

    private String build;
    private String revision;
    private int days;
    private int numberOfTestcaseExecuted;
    private int numberOfApplicationExecuted;
    private int total;
    private int numberOfOK;
    private int numberOfKO;
    private int numberOfExecPerTc;
    private int numberOfExecPerTcPerDay;
    private int percentageOfOK;

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getNumberOfExecPerTc() {
        return numberOfExecPerTc;
    }

    public void setNumberOfExecPerTc(int numberOfExecPerTc) {
        this.numberOfExecPerTc = numberOfExecPerTc;
    }

    public int getNumberOfExecPerTcPerDay() {
        return numberOfExecPerTcPerDay;
    }

    public void setNumberOfExecPerTcPerDay(int numberOfExecPerTcPerDay) {
        this.numberOfExecPerTcPerDay = numberOfExecPerTcPerDay;
    }

    public int getNumberOfKO() {
        return numberOfKO;
    }

    public void setNumberOfKO(int numberOfKO) {
        this.numberOfKO = numberOfKO;
    }

    public int getNumberOfOK() {
        return numberOfOK;
    }

    public void setNumberOfOK(int numberOfOK) {
        this.numberOfOK = numberOfOK;
    }

    public int getNumberOfTestcaseExecuted() {
        return numberOfTestcaseExecuted;
    }

    public void setNumberOfTestcaseExecuted(int numberOfTestcaseExecuted) {
        this.numberOfTestcaseExecuted = numberOfTestcaseExecuted;
    }

    public int getNumberOfApplicationExecuted() {
        return numberOfApplicationExecuted;
    }

    public void setNumberOfApplicationExecuted(int numberOfApplicationExecuted) {
        this.numberOfApplicationExecuted = numberOfApplicationExecuted;
    }

    public float getPercentageOfOK() {
        return percentageOfOK;
    }

    public void setPercentageOfOK(int percentageOfOK) {
        this.percentageOfOK = percentageOfOK;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}