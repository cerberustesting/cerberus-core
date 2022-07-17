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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author vertigo17
 */
@Data
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
    private String xRayTestExecution;
    private String xRayURL;
    private String lambdaTestBuild;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    /*
     * Outside Database Model
     */
    @EqualsAndHashCode.Exclude
    private List<TestCaseExecution> executionsNew;

    public void appendExecutions(TestCaseExecution executions) {
        this.executionsNew.add(executions);
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
            result.put("xRayTestExecution", this.xRayTestExecution);
            result.put("xRayURL", this.xRayURL);

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
     * @param prioritiesList : send the invariant list of priorities to the
     * method (this is to avoid getting value from database for every entries)
     * @param countriesList : send the invariant list of countries to the method
     * (this is to avoid getting value from database for every entries)
     * @param environmentsList : send the invariant list of environments to the
     * method (this is to avoid getting value from database for every entries)
     * @return
     */
    public JSONObject toJsonV001(String cerberusURL, List<Invariant> prioritiesList, List<Invariant> countriesList, List<Invariant> environmentsList) {
        JSONObject result = new JSONObject();
        try {
            result.put("JSONVersion", "001");
            cerberusURL = StringUtil.addSuffixIfNotAlready(cerberusURL, "/");
            result.put("link", cerberusURL + "ReportingExecutionByTag.jsp?Tag=" + URLEncoder.encode(this.tag, "UTF-8"));
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

        } catch (JSONException | UnsupportedEncodingException ex) {
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
