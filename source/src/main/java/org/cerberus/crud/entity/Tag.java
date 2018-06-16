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
package org.cerberus.crud.entity;

import java.sql.Timestamp;

/**
 * @author vertigo
 */
public class Tag {

    private long id;
    private String tag;
    private String description;
    private String campaign;
    private Timestamp DateEndQueue;
    private int nbExe;
    private int nbExeUsefull;
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
    private int ciScore;
    private int ciScoreThreshold;
    private String ciResult;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public Timestamp getDateEndQueue() {
        return DateEndQueue;
    }

    public void setDateEndQueue(Timestamp DateEndQueue) {
        this.DateEndQueue = DateEndQueue;
    }

    public int getNbExe() {
        return nbExe;
    }

    public void setNbExe(int nbExe) {
        this.nbExe = nbExe;
    }

    public int getNbExeUsefull() {
        return nbExeUsefull;
    }

    public void setNbExeUsefull(int nbExeUsefull) {
        this.nbExeUsefull = nbExeUsefull;
    }

    public int getNbOK() {
        return nbOK;
    }

    public void setNbOK(int nbOK) {
        this.nbOK = nbOK;
    }

    public int getNbKO() {
        return nbKO;
    }

    public void setNbKO(int nbKO) {
        this.nbKO = nbKO;
    }

    public int getNbFA() {
        return nbFA;
    }

    public void setNbFA(int nbFA) {
        this.nbFA = nbFA;
    }

    public int getNbNA() {
        return nbNA;
    }

    public void setNbNA(int nbNA) {
        this.nbNA = nbNA;
    }

    public int getNbNE() {
        return nbNE;
    }

    public void setNbNE(int nbNE) {
        this.nbNE = nbNE;
    }

    public int getNbWE() {
        return nbWE;
    }

    public void setNbWE(int nbWE) {
        this.nbWE = nbWE;
    }

    public int getNbPE() {
        return nbPE;
    }

    public void setNbPE(int nbPE) {
        this.nbPE = nbPE;
    }

    public int getNbQU() {
        return nbQU;
    }

    public void setNbQU(int nbQU) {
        this.nbQU = nbQU;
    }

    public int getNbQE() {
        return nbQE;
    }

    public void setNbQE(int nbQE) {
        this.nbQE = nbQE;
    }

    public int getNbCA() {
        return nbCA;
    }

    public void setNbCA(int nbCA) {
        this.nbCA = nbCA;
    }

    public int getCiScore() {
        return ciScore;
    }

    public void setCiScore(int ciScore) {
        this.ciScore = ciScore;
    }

    public int getCiScoreThreshold() {
        return ciScoreThreshold;
    }

    public void setCiScoreThreshold(int ciScoreThreshold) {
        this.ciScoreThreshold = ciScoreThreshold;
    }

    public String getCiResult() {
        return ciResult;
    }

    public void setCiResult(String ciResult) {
        this.ciResult = ciResult;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(Timestamp DateCreated) {
        this.DateCreated = DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public Timestamp getDateModif() {
        return DateModif;
    }

    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasSameKey(Tag obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Tag other = (Tag) obj;
        if ((this.tag == null) ? (other.tag != null) : !this.tag.equals(other.tag)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 67 * hash + (this.tag != null ? this.tag.hashCode() : 0);
        hash = 67 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 67 * hash + (this.campaign != null ? this.campaign.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tag other = (Tag) obj;
        if ((this.tag == null) ? (other.tag != null) : !this.tag.equals(other.tag)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.campaign == null) ? (other.campaign != null) : !this.campaign.equals(other.campaign)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return tag;
    }
}
