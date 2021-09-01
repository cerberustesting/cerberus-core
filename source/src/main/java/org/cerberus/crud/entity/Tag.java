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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author vertigo17
 */
public class Tag {

    private static final Logger LOG = LogManager.getLogger(Tag.class);

    private long id;
    private String tag;
    private String description;
    private String comment;
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
    private String environmentList;
    private String countryList;
    private String robotDecliList;
    private String systemList;
    private String applicationList;
    private String reqEnvironmentList;
    private String reqCountryList;
    private String browserstackBuildHash;
    private String lambdaTestBuild;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    /*
     * Outside Database Model
     */
    private List<TestCaseExecution> executionsNew;

    public List<TestCaseExecution> getExecutions() {
        return executionsNew;
    }

    public void setExecutions(List<TestCaseExecution> executions) {
        this.executionsNew = executions;
    }

    public void appendExecutions(TestCaseExecution executions) {
        this.executionsNew.add(executions);
    }

    public String getLambdaTestBuild() {
        return lambdaTestBuild;
    }

    public void setLambdaTestBuild(String lambdaTestBuild) {
        this.lambdaTestBuild = lambdaTestBuild;
    }

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

    public String getEnvironmentList() {
        return environmentList;
    }

    public void setEnvironmentList(String environmentList) {
        this.environmentList = environmentList;
    }

    public String getCountryList() {
        return countryList;
    }

    public void setCountryList(String countryList) {
        this.countryList = countryList;
    }

    public String getRobotDecliList() {
        return robotDecliList;
    }

    public void setRobotDecliList(String robotDecliList) {
        this.robotDecliList = robotDecliList;
    }

    public String getSystemList() {
        return systemList;
    }

    public void setSystemList(String systemList) {
        this.systemList = systemList;
    }

    public String getApplicationList() {
        return applicationList;
    }

    public void setApplicationList(String applicationList) {
        this.applicationList = applicationList;
    }

    public String getReqEnvironmentList() {
        return reqEnvironmentList;
    }

    public void setReqEnvironmentList(String reqEnvironmentList) {
        this.reqEnvironmentList = reqEnvironmentList;
    }

    public String getReqCountryList() {
        return reqCountryList;
    }

    public void setReqCountryList(String reqCountryList) {
        this.reqCountryList = reqCountryList;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBrowserstackBuildHash() {
        return browserstackBuildHash;
    }

    public void setBrowserstackBuildHash(String browserstackBuildHash) {
        this.browserstackBuildHash = browserstackBuildHash;
    }

    public boolean hasSameKey(Tag obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Tag other = obj;
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

    public JSONObject toJsonLight() {
        JSONObject result = new JSONObject();
        try {
            result.put("tag", this.tag);
            result.put("campaign", this.campaign);
            result.put("description", this.description);
            result.put("browserstackBuildHash", this.browserstackBuildHash);
            result.put("lambdaTestBuild", this.lambdaTestBuild);
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            
            result.put("id", this.id);
            result.put("tag", this.tag);
            result.put("campaign", this.campaign);
            result.put("description", this.description);
            result.put("comment", this.comment);
            result.put("DateEndQueue", this.DateEndQueue);
            result.put("nbExe", this.nbExe);
            result.put("nbExeUsefull", this.nbExeUsefull);
            result.put("nbOK", this.nbOK);
            result.put("nbKO", this.nbKO);
            result.put("nbFA", this.nbFA);
            result.put("nbNA", this.nbNA);
            result.put("nbNE", this.nbNE);
            result.put("nbWE", this.nbWE);
            result.put("nbPE", this.nbPE);
            result.put("nbQU", this.nbQU);
            result.put("nbQE", this.nbQE);
            result.put("nbCA", this.nbCA);
            result.put("ciScore", this.ciScore);
            result.put("ciScoreThreshold", this.ciScoreThreshold);
            result.put("ciResult", this.ciResult);
            result.put("environmentList", this.environmentList);
            result.put("countryList", this.countryList);
            result.put("robotDecliList", this.robotDecliList);
            result.put("systemList", this.systemList);
            result.put("applicationList", this.applicationList);
            result.put("reqEnvironmentList", this.reqEnvironmentList);
            result.put("reqCountryList", this.reqCountryList);
            result.put("UsrCreated", this.UsrCreated);
            result.put("DateCreated", this.DateCreated);
            result.put("UsrModif", this.UsrModif);
            result.put("DateModif", this.DateModif);
            result.put("browserstackBuildHash", this.browserstackBuildHash);
            result.put("lambdaTestBuild", this.lambdaTestBuild);
            
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }
    
    /**
     *
     * @param cerberusURL
     * @param prioritiesList : send the invariant list of priorities to the method (this is to avoid getting value from database for every entries)
     * @param countriesList : send the invariant list of countries to the method (this is to avoid getting value from database for every entries)
     * @param environmentsList : send the invariant list of environments to the method (this is to avoid getting value from database for every entries)
     * @return
     */
    public JSONObject toJsonV001(String cerberusURL, List<Invariant> prioritiesList, List<Invariant> countriesList, List<Invariant> environmentsList) {
        JSONObject result = new JSONObject();
        try {
            result.put("JSONVersion", "001");
            cerberusURL = StringUtil.addSuffixIfNotAlready(cerberusURL, "/");
            result.put("link", cerberusURL + "ReportingExecutionByTag.jsp?Tag=" + URLEncoder.encode(this.tag, StandardCharsets.UTF_8));
            result.put("tag", this.tag);
            if (this.DateEndQueue != null && this.DateCreated != null) {
                result.put("tagDurationInMs", (this.DateEndQueue.getTime() - this.DateCreated.getTime()));
            }
            result.put("CI", this.ciResult);
            result.put("start", this.DateCreated);
            result.put("end", this.DateEndQueue);
            result.put("campaign", this.campaign);
            result.put("description", this.description);
            result.put("browserstackBuildHash", this.browserstackBuildHash);
            result.put("lambdaTestBuild", this.lambdaTestBuild);
            JSONObject result1 = new JSONObject();
            result1.put("OK", this.nbOK);
            result1.put("KO", this.nbKO);
            result1.put("FA", this.nbFA);
            result1.put("NA", this.nbNA);
            result1.put("PE", this.nbPE);
            result1.put("CA", this.nbCA);
            result1.put("QU", this.nbQU);
            result1.put("WE", this.nbWE);
            result1.put("NE", this.nbNE);
            result1.put("QE", this.nbQE);
            result1.put("total", this.nbExeUsefull);
            result1.put("totalWithRetry", this.nbExe);
            result.put("results", result1);
            JSONArray listOfExecutionsJSON = new JSONArray();
            for (TestCaseExecution execution : executionsNew) {
                listOfExecutionsJSON.put(execution.toJsonV001(cerberusURL, prioritiesList, countriesList, environmentsList));
            }
            result.put("executions", listOfExecutionsJSON);

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    @Override
    public String toString() {
        return tag;
    }
}
