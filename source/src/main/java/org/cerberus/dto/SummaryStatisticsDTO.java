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

import java.text.DecimalFormat;

 /**
     * Class that creates a row with statistics (total values and percentage values)
     */
public class SummaryStatisticsDTO {

        private String application;
        private String country;
        private String environment;
        private int ok;
        private int ko;
        private int fa;
        private int na;
        private int ne;
        private int pe;
        private int ca;
        private int total;
        private int notOkTotal;
        private float percOk;
        private float percKo;
        private float percNa;
        private float percNe;
        private float percPe;
        private float percFa;
        private float percCa;
        private float percNotOkTotal;

        
        public SummaryStatisticsDTO() {
            this.application = "";
            this.country = "";
            this.environment = "";
            this.ok = 0;
            this.ko = 0;
            this.fa = 0;
            this.na = 0;
            this.ne = 0;
            this.pe = 0;            
            this.ca = 0;
            this.total = 0;
            this.notOkTotal = 0;
        }

        public void updateStatisticByStatus(String status) {
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            
            this.total++;
            if (status.equalsIgnoreCase("OK")) {
                this.ok++;
                this.percOk = Float.valueOf(twoDForm.format((float)(this.ok * 100)/this.total));
            } else if (status.equalsIgnoreCase("KO")) {
                this.ko++;
                this.percKo = Float.valueOf(twoDForm.format((float)(this.ko * 100)/this.total));
                this.notOkTotal++;
            } else if (status.equalsIgnoreCase("NA")) {
                this.na++;
                this.percNa = Float.valueOf(twoDForm.format((float)(this.na * 100)/this.total));
                this.notOkTotal++;                
            } else if (status.equalsIgnoreCase("NE")) {
                this.ne++;
                this.percNe = Float.valueOf(twoDForm.format((float)(this.ne * 100)/this.total));
                this.notOkTotal++;                
            } else if (status.equalsIgnoreCase("PE")) {
                this.pe++;
                this.percPe = Float.valueOf(twoDForm.format((float)(this.pe * 100)/this.total));
                this.notOkTotal++;                
            } else if (status.equalsIgnoreCase("FA")) {
                this.fa++;
                this.percFa = Float.valueOf(twoDForm.format((float)(this.fa * 100)/this.total));
                this.notOkTotal++;                
            } else if (status.equalsIgnoreCase("CA")) {
                this.ca++;
                this.percCa = Float.valueOf(twoDForm.format((float)(this.ca * 100)/this.total));
                this.notOkTotal++;
            }
            this.percNotOkTotal  = Math.round((float)(this.notOkTotal *100)/this.total);
            
        }
        public void updatePercentageStatistics() {
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            this.percOk = Float.valueOf(twoDForm.format((float)(this.ok * 100)/this.total));         
            this.percKo = Float.valueOf(twoDForm.format((float)(this.ko * 100)/this.total));         
            this.percFa = Float.valueOf(twoDForm.format((float)(this.fa * 100)/this.total));         
            this.percNa = Float.valueOf(twoDForm.format((float)(this.na * 100)/this.total));         
            this.percNe = Float.valueOf(twoDForm.format((float)(this.ne * 100)/this.total));         
            this.percPe = Float.valueOf(twoDForm.format((float)(this.pe * 100)/this.total));         
            this.percCa = Float.valueOf(twoDForm.format((float)(this.ca * 100)/this.total));         
            this.percNotOkTotal  = Math.round(((float)(this.notOkTotal*100)/this.total));
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
        
        public int getOk() {
            return ok;
        }

        public void setOk(int ok) {
            this.ok = ok;
        }

        public int getKo() {
            return ko;
        }

        public void setKo(int ko) {
            this.ko = ko;
        }

        public int getNa() {
            return na;
        }

        public void setNa(int na) {
            this.na = na;
        }

        public int getPe() {
            return pe;
        }

        public void setPe(int pe) {
            this.pe = pe;
        }

        public int getFa() {
            return fa;
        }

        public void setFa(int fa) {
            this.fa = fa;
        }

        public int getNe() {
            return ne;
        }


        public int getCa() {
            return ca;
        }
        
        public int getTotal() {
            return total;
        }

        public int getNotOkTotal() {
            return notOkTotal;
        }
        
        public float getPercOk() {
            return percOk;
        }

        public float getPercKo() {
            return percKo;
        }

        public float getPercNa() {
            return percNa;
        }

        public float getPercNe() {
            return percNe;
        }
        
        public float getPercPe() {
            return percPe;
        }

        public float getPercFa() {
            return percFa;
        }

        public float getPercCa() {
            return percCa;
        }

        public float getPercNotOkTotal() {
            return percNotOkTotal;
        }
        public void setTotal(int total) {
            this.total = total;
        }

        public void setNotOkTotal(int notOkTotal) {
            this.notOkTotal = notOkTotal;
        }

        public void setPercOk(float percOk) {
            this.percOk = percOk;
        }

        public void setPercKo(float percKo) {
            this.percKo = percKo;
        }

        public void setPercNa(float percNa) {
            this.percNa = percNa;
        }

        public void setPercPe(float percPe) {
            this.percPe = percPe;
        }

        public void setPercFa(float percFa) {
            this.percFa = percFa;
        }

        public void setPercCa(float percCa) {
            this.percCa = percCa;
        }

        public void setPercNotOkTotal(float percNotOkTotal) {
            this.percNotOkTotal = percNotOkTotal;
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
        
        public void setNe(int ne) {
            this.ne = ne;
        }

        public void setCa(int ca) {
            this.ca = ca;
        }
    }