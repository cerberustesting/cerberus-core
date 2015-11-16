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
package org.cerberus.dto;

 /**
     * Class that creates a row with statistics (total values and percentage values)
     */
public class SummaryStatisticsDTO {

        private String application;
        private String country;
        private String environment;
        private String browser;
        private int OK;
        private int KO;
        private int FA;
        private int NA;
        private int NE;
        private int PE;
        private int CA;
        private int total;
        private int notOKTotal;
        private float percOK;
        private float percKO;
        private float percNA;
        private float percNE;
        private float percPE;
        private float percFA;
        private float percCA;
        private float percNotOKTotal;

        
        public SummaryStatisticsDTO() {
            this.application = "";
            this.country = "";
            this.environment = "";
            this.browser = "";
            this.OK = 0;
            this.KO = 0;
            this.FA = 0;
            this.NA = 0;
            this.NE = 0;
            this.PE = 0;            
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
            } else if (status.equalsIgnoreCase("PE")) {
                this.PE++;
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
            this.percOK = (float)Math.round(((float)(this.OK * 100)/this.total) * 10) / 10;         
            this.percKO = (float)Math.round(((float)(this.KO * 100)/this.total) * 10) / 10;         
            this.percFA = (float)Math.round(((float)(this.FA * 100)/this.total) * 10) / 10;         
            this.percNA = (float)Math.round(((float)(this.NA * 100)/this.total) * 10) / 10;         
            this.percNE = (float)Math.round(((float)(this.NE * 100)/this.total) * 10) / 10;         
            this.percPE = (float)Math.round(((float)(this.PE * 100)/this.total) * 10) / 10;         
            this.percCA = (float)Math.round(((float)(this.CA * 100)/this.total) * 10) / 10;         
            this.percNotOKTotal  = (float)Math.round(((float)(this.notOKTotal*100)/this.total) * 10) / 10;
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
        
        public String getBrowser() {
            return browser;
        }
        
        public int getOk() {
            return OK;
        }

        public void setOk(int ok) {
            this.OK = ok;
        }

        public int getKo() {
            return KO;
        }

        public void setKo(int ko) {
            this.KO = ko;
        }

        public int getNa() {
            return NA;
        }

        public void setNa(int na) {
            this.NA = na;
        }

        public int getPe() {
            return PE;
        }

        public void setPe(int pe) {
            this.PE = pe;
        }

        public int getFa() {
            return FA;
        }

        public void setFa(int fa) {
            this.FA = fa;
        }

        public int getNe() {
            return NE;
        }


        public int getCa() {
            return CA;
        }
        
        public int getTotal() {
            return total;
        }

        public int getNotOkTotal() {
            return notOKTotal;
        }
        
        public float getPercOk() {
            return percOK;
        }

        public float getPercKo() {
            return percKO;
        }

        public float getPercNa() {
            return percNA;
        }

        public float getPercNe() {
            return percNE;
        }
        
        public float getPercPe() {
            return percPE;
        }

        public float getPercFa() {
            return percFA;
        }

        public float getPercCa() {
            return percCA;
        }

        public float getPercNotOkTotal() {
            return percNotOKTotal;
        }
        public void setTotal(int total) {
            this.total = total;
        }

        public void setNotOkTotal(int notOkTotal) {
            this.notOKTotal = notOkTotal;
        }

        public void setPercOk(float percOk) {
            this.percOK = percOk;
        }

        public void setPercKo(float percKo) {
            this.percKO = percKo;
        }

        public void setPercNa(float percNa) {
            this.percNA = percNa;
        }

        public void setPercPe(float percPe) {
            this.percPE = percPe;
        }

        public void setPercFa(float percFa) {
            this.percFA = percFa;
        }

        public void setPercCa(float percCa) {
            this.percCA = percCa;
        }

        public void setPercNotOkTotal(float percNotOkTotal) {
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
        
        public void setBrowser(String browser) {
            this.browser = browser;
        }
        
        public void setNe(int ne) {
            this.NE = ne;
        }

        public void setCa(int ca) {
            this.CA = ca;
        }
    }