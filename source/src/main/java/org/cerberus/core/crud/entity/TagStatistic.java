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
package org.cerberus.core.crud.entity;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TagStatistic {
    private long id;
    private String tag;
    private String country;
    private String environment;
    private String campaign;
    private String campaignGroup1;
    private String systemList;
    private String applicationList;
    private Timestamp dateStartExe;
    private Timestamp dateEndExe;
    private int nbExe;
    private int nbExeUseful;
    private int nbOK;
    private int nbKO;
    private int nbFA;
    private int nbNA;
    private int nbNE;
    private int nbWE;
    private int nbPE;
    private int nbQU;
    private int nbQE;
    private int nbCA;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    private List<TestCaseExecution> executions;

    public void incrementNbOK() {
        this.nbOK++;
    }

    public void incrementNbFA() {
        this.nbFA++;
    }

    public void incrementNbKO() {
        this.nbKO++;
    }

    public void incrementNbNA() {
        this.nbNA++;
    }

    public void incrementNbNE() {
        this.nbNE++;
    }

    public void incrementNbWE() {
        this.nbWE++;
    }

    public void incrementNbPE() {
        this.nbPE++;
    }

    public void incrementNbQU() {
        this.nbQU++;
    }

    public void incrementNbQE() {
        this.nbQE++;
    }

    public void incrementNbCA() {
        this.nbCA++;
    }
}
