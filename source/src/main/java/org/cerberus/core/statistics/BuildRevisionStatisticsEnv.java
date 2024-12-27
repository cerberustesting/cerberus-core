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
package org.cerberus.core.statistics;

/**
 *
 * @author bcivel
 */
public class BuildRevisionStatisticsEnv {

    private String system;
    private String build;
    private String revision;
    private int nbEnvDEV;
    private int nbEnvQA;
    private int nbEnvUAT;
    private int nbEnvPROD;

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public int getNbEnvDEV() {
        return nbEnvDEV;
    }

    public void setNbEnvDEV(int nbEnvDEV) {
        this.nbEnvDEV = nbEnvDEV;
    }

    public int getNbEnvQA() {
        return nbEnvQA;
    }

    public void setNbEnvQA(int nbEnvQA) {
        this.nbEnvQA = nbEnvQA;
    }

    public int getNbEnvUAT() {
        return nbEnvUAT;
    }

    public void setNbEnvUAT(int nbEnvUAT) {
        this.nbEnvUAT = nbEnvUAT;
    }

    public int getNbEnvPROD() {
        return nbEnvPROD;
    }

    public void setNbEnvPROD(int nbEnvPROD) {
        this.nbEnvPROD = nbEnvPROD;
    }

}
