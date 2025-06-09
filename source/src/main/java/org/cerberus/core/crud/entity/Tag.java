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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * @author vertigo17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Tag {

    private static final Logger LOG = LogManager.getLogger(Tag.class);

    private long id;
    private String tag;
    private String description;
    private String comment;
    private String campaign;
    private Timestamp dateEndQueue;
    private Timestamp dateStartExe;
    private long durationMs;
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
    private int nbPA;
    private int nbQE;
    private int nbCA;
    private int ciScore;
    private int ciScoreThreshold;
    private int ciScoreMax;
    private String ciResult;
    private boolean falseNegative;
    private String falseNegativeRootCause;
    private int nbFlaky;
    private int nbMuted;
    private String environmentList;
    private String countryList;
    private String robotDecliList;
    private String systemList;
    private String applicationList;
    private String reqEnvironmentList;
    private String reqCountryList;
    private String browserstackBuildHash;
    private String browserstackAppBuildHash;
    private String xRayTestExecution;
    private String xRayURL;
    private String xRayMessage;
    private String lambdaTestBuild;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    // Outside Database Model
    @EqualsAndHashCode.Exclude
    private List<TestCaseExecution> executionsNew;

    public boolean hasSameKey(Tag obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.tag, obj.tag);
    }

    public JSONObject toJsonLight() {
        JSONObject result = new JSONObject();
        try {
            result.put("tag", this.tag);
            result.put("campaign", this.campaign);
            result.put("description", this.description);
            result.put("browserstackBuildHash", this.browserstackBuildHash);
            result.put("browserstackAppBuildHash", this.browserstackAppBuildHash);
            result.put("lambdaTestBuild", this.lambdaTestBuild);
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
            result.put("DateEndQueue", this.dateEndQueue);
            result.put("DateStartExe", this.dateStartExe);
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
            result.put("nbPA", this.nbPA);
            result.put("nbQE", this.nbQE);
            result.put("nbCA", this.nbCA);
            result.put("ciScore", this.ciScore);
            result.put("ciScoreThreshold", this.ciScoreThreshold);
            result.put("ciScoreMax", this.ciScoreMax);
            result.put("ciResult", this.ciResult);
            result.put("falseNegative", this.falseNegative);
            result.put("falseNegativeRootCause", this.falseNegativeRootCause);
            result.put("nbFlaky", this.nbFlaky);
            result.put("nbMuted", this.nbMuted);
            result.put("environmentList", this.environmentList);
            result.put("countryList", this.countryList);
            result.put("robotDecliList", this.robotDecliList);
            result.put("systemList", this.systemList);
            result.put("applicationList", this.applicationList);
            result.put("reqEnvironmentList", this.reqEnvironmentList);
            result.put("reqCountryList", this.reqCountryList);
            result.put("UsrCreated", this.usrCreated);
            result.put("DateCreated", this.dateCreated);
            result.put("UsrModif", this.usrModif);
            result.put("DateModif", this.dateModif);
            result.put("browserstackBuildHash", this.browserstackBuildHash);
            result.put("browserstackAppBuildHash", this.browserstackAppBuildHash);
            result.put("lambdaTestBuild", this.lambdaTestBuild);
            result.put("xRayTestExecution", this.xRayTestExecution);
            result.put("xRayURL", this.xRayURL);
            result.put("xRayMessage", this.xRayMessage);

        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

    /**
     * @param cerberusURL
     * @param prioritiesList   : send the invariant list of priorities to the
     *                         method (this is to avoid getting value from database for every entries)
     * @param countriesList    : send the invariant list of countries to the method
     *                         (this is to avoid getting value from database for every entries)
     * @param environmentsList : send the invariant list of environments to the
     *                         method (this is to avoid getting value from database for every entries)
     * @return
     */
    public JSONObject toJsonV001(String cerberusURL, List<Invariant> prioritiesList, List<Invariant> countriesList, List<Invariant> environmentsList) {
        JSONObject result = new JSONObject();
        try {
            result.put("JSONVersion", "001");
            cerberusURL = StringUtil.addSuffixIfNotAlready(cerberusURL, "/");
            result.put("link", cerberusURL + "ReportingExecutionByTag.jsp?Tag=" + StringUtil.encodeURL(this.tag));
            result.put("tag", this.tag);
            if (this.dateEndQueue != null && this.dateStartExe != null) {
                result.put("tagDurationInMs", (this.dateEndQueue.getTime() - this.dateStartExe.getTime()));
            }
            result.put("CI", this.ciResult);
            result.put("falseNegative", this.falseNegative);
            result.put("start", this.dateCreated);
            result.put("startExe", this.dateStartExe);
            result.put("end", this.dateEndQueue);
            result.put("campaign", this.campaign);
            result.put("description", this.description);
            result.put("browserstackBuildHash", this.browserstackBuildHash);
            result.put("browserstackAppBuildHash", this.browserstackAppBuildHash);
            result.put("lambdaTestBuild", this.lambdaTestBuild);
            JSONObject result1 = new JSONObject();
            result1.put("OK", this.nbOK);
            result1.put("KO", this.nbKO);
            result1.put("FA", this.nbFA);
            result1.put("NA", this.nbNA);
            result1.put("PE", this.nbPE);
            result1.put("CA", this.nbCA);
            result1.put("QU", this.nbQU);
            result1.put("PA", this.nbPA);
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
