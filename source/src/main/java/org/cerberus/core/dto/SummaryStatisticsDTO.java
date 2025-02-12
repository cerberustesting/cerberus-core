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

import org.json.JSONObject;

/**
 * Class that creates a row with statistics (total values and percentage values)
 */
public class SummaryStatisticsDTO {

    private String application;
    private String country;
    private String environment;
    private String robotDecli;
    private JSONObject label;
    private int OK;
    private int KO;
    private int FA;
    private int NA;
    private int NE;
    private int WE;
    private int PE;
    private int QE;
    private int QU;
    private int PA;
    private int CA;
    private int total;
    private int notOKTotal;
    private float percOK;
    private float percKO;
    private float percFA;
    private float percNA;
    private float percNE;
    private float percWE;
    private float percPE;
    private float percQE;
    private float percQU;
    private float percPA;
    private float percCA;
    private float percNotOKTotal;

    public SummaryStatisticsDTO() {
        this.application = "";
        this.country = "";
        this.environment = "";
        this.robotDecli = "";
        this.OK = 0;
        this.KO = 0;
        this.FA = 0;
        this.NA = 0;
        this.NE = 0;
        this.WE = 0;
        this.PE = 0;
        this.QE = 0;
        this.QU = 0;
        this.PA = 0;
        this.CA = 0;
        this.total = 0;
        this.notOKTotal = 0;
    }

    public void updateStatisticByStatus(String status) {
        this.total++;
        if (status.equalsIgnoreCase("OK")) {
            this.OK++;
        } else if (status.equalsIgnoreCase("KO")) {
            this.KO++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("NA")) {
            this.NA++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("NE")) {
            this.NE++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("WE")) {
            this.WE++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("PE")) {
            this.PE++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("QE")) {
            this.QE++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("QU")) {
            this.QU++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("PA")) {
            this.PA++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("FA")) {
            this.FA++;
            this.notOKTotal++;
        } else if (status.equalsIgnoreCase("CA")) {
            this.CA++;
            this.notOKTotal++;
        }
    }

    public void updatePercentageStatistics() {
        this.percOK = (float) Math.round(((float) (this.OK * 100) / this.total) * 10) / 10;
        this.percKO = (float) Math.round(((float) (this.KO * 100) / this.total) * 10) / 10;
        this.percFA = (float) Math.round(((float) (this.FA * 100) / this.total) * 10) / 10;
        this.percNA = (float) Math.round(((float) (this.NA * 100) / this.total) * 10) / 10;
        this.percNE = (float) Math.round(((float) (this.NE * 100) / this.total) * 10) / 10;
        this.percWE = (float) Math.round(((float) (this.WE * 100) / this.total) * 10) / 10;
        this.percPE = (float) Math.round(((float) (this.PE * 100) / this.total) * 10) / 10;
        this.percQE = (float) Math.round(((float) (this.QE * 100) / this.total) * 10) / 10;
        this.percQU = (float) Math.round(((float) (this.QU * 100) / this.total) * 10) / 10;
        this.percPA = (float) Math.round(((float) (this.PA * 100) / this.total) * 10) / 10;
        this.percCA = (float) Math.round(((float) (this.CA * 100) / this.total) * 10) / 10;
        this.percNotOKTotal = (float) Math.round(((float) (this.notOKTotal * 100) / this.total) * 10) / 10;
    }

    public String getApplication() {
        return application;
    }

    public String getCountry() {
        return country;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getRobotDecli() {
        return robotDecli;
    }

    public int getPA() {
        return PA;
    }

    public void setPA(int PA) {
        this.PA = PA;
    }

    public float getPercPA() {
        return percPA;
    }

    public void setPercPA(float percPA) {
        this.percPA = percPA;
    }

    public int getWE() {
        return WE;
    }

    public void setWE(int WE) {
        this.WE = WE;
    }

    public float getPercWE() {
        return percWE;
    }

    public void setPercWE(float percWE) {
        this.percWE = percWE;
    }

    public int getQE() {
        return QE;
    }

    public void setQE(int QE) {
        this.QE = QE;
    }

    public float getPercQE() {
        return percQE;
    }

    public void setPercQE(float percQE) {
        this.percQE = percQE;
    }

    public int getOK() {
        return OK;
    }

    public void setOK(int ok) {
        this.OK = ok;
    }

    public int getQU() {
        return QU;
    }

    public void setQU(int QU) {
        this.QU = QU;
    }

    public float getPercQU() {
        return percQU;
    }

    public void setPercQU(float percQU) {
        this.percQU = percQU;
    }

    public int getKO() {
        return KO;
    }

    public void setKO(int ko) {
        this.KO = ko;
    }

    public int getNA() {
        return NA;
    }

    public void setNA(int na) {
        this.NA = na;
    }

    public int getPE() {
        return PE;
    }

    public void setPE(int pe) {
        this.PE = pe;
    }

    public int getFA() {
        return FA;
    }

    public void setFA(int fa) {
        this.FA = fa;
    }

    public int getNE() {
        return NE;
    }

    public int getCA() {
        return CA;
    }

    public int getTotal() {
        return total;
    }

    public int getNotOKTotal() {
        return notOKTotal;
    }

    public float getPercOK() {
        return percOK;
    }

    public float getPercKO() {
        return percKO;
    }

    public float getPercNA() {
        return percNA;
    }

    public float getPercNE() {
        return percNE;
    }

    public float getPercPE() {
        return percPE;
    }

    public float getPercFA() {
        return percFA;
    }

    public float getPercCA() {
        return percCA;
    }

    public float getPercNotOKTotal() {
        return percNotOKTotal;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setNotOKTotal(int notOkTotal) {
        this.notOKTotal = notOkTotal;
    }

    public void setPercOK(float percOk) {
        this.percOK = percOk;
    }

    public void setPercKO(float percKo) {
        this.percKO = percKo;
    }

    public void setPercNA(float percNa) {
        this.percNA = percNa;
    }

    public void setPercPE(float percPe) {
        this.percPE = percPe;
    }

    public void setPercFA(float percFa) {
        this.percFA = percFa;
    }

    public void setPercCA(float percCa) {
        this.percCA = percCa;
    }

    public void setPercNotOKTotal(float percNotOkTotal) {
        this.percNotOKTotal = percNotOkTotal;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setRobotDecli(String robotDecli) {
        this.robotDecli = robotDecli;
    }

    public void setNE(int ne) {
        this.NE = ne;
    }

    public void setCA(int ca) {
        this.CA = ca;
    }

    public JSONObject getLabel() {
        return label;
    }

    public void setLabel(JSONObject label) {
        this.label = label;
    }
    
    
}
