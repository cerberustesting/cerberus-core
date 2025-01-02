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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.engine.entity.MessageEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * @author bcivel
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class TestCaseCountryProperties {

    private String test;
    private String testcase;
    private String country;
    private String property;
    private String description;
    private String type;
    private String database;
    private String value1;
    private String value2;
    private String value3;
    private String length;
    private int rowLimit;
    private String nature;
    private int cacheExpire;
    private int retryNb;
    private int retryPeriod;
    private int rank;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;

    /**
     * From here are data outside database model.
     */
    @EqualsAndHashCode.Exclude
    private MessageEvent result;
    @EqualsAndHashCode.Exclude
    private TestCaseCountry testcaseCountry;
    @EqualsAndHashCode.Exclude
    private List<Invariant> invariantCountries;
    @EqualsAndHashCode.Exclude
    private List<TestCaseCountry> testcaseCountries;

    private static final Logger LOG = LogManager.getLogger(TestCaseCountryProperties.class);

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_GETFROMDATALIB = "getFromDataLib";
    public static final String TYPE_GETFROMSQL = "getFromSql";
    public static final String TYPE_GETFROMHTML = "getFromHtml";
    public static final String TYPE_GETFROMHTMLVISIBLE = "getFromHtmlVisible";
    public static final String TYPE_GETFROMJS = "getFromJS";
    public static final String TYPE_GETATTRIBUTEFROMHTML = "getAttributeFromHtml";
    public static final String TYPE_GETFROMCOOKIE = "getFromCookie";
    public static final String TYPE_GETFROMXML = "getFromXml";
    public static final String TYPE_GETRAWFROMXML = "getRawFromXml";
    public static final String TYPE_GETDIFFERENCESFROMXML = "getDifferencesFromXml";
    public static final String TYPE_GETFROMJSON = "getFromJson";
    public static final String TYPE_GETRAWFROMJSON = "getRawFromJson";
    public static final String TYPE_GETFROMGROOVY = "getFromGroovy";
    public static final String TYPE_GETFROMCOMMAND = "getFromCommand";
    public static final String TYPE_GETELEMENTPOSITION = "getElementPosition";
    public static final String TYPE_GETFROMNETWORKTRAFFIC = "getFromNetworkTraffic";
    public static final String TYPE_GETOTP = "getOTP";
    public static final String TYPE_GETFROMEXECUTIONOBJECT = "getFromExecutionObject";

    // DEPRECATED
    /**
     * @deprecated
     */
    @Deprecated
    public static final String TYPE_EXECUTESQLFROMLIB = "executeSqlFromLib";

    /**
     * @deprecated
     */
    @Deprecated
    public static final String TYPE_EXECUTESOAPFROMLIB = "executeSoapFromLib";
    /**
     * Invariant PROPERTY NATURE String.
     */
    public static final String NATURE_STATIC = "STATIC";
    public static final String NATURE_RANDOM = "RANDOM";
    public static final String NATURE_RANDOMNEW = "RANDOMNEW";
    public static final String NATURE_NOTINUSE = "NOTINUSE";

    /**
     * Invariant Value3  String.
     */
    public static final String VALUE3_VALUE = "value";
    public static final String VALUE3_RAW = "raw";
    public static final String VALUE3_COORDINATE = "coordinate";
    public static final String VALUE3_COUNT = "count";
    public static final String VALUE3_VALUESUM = "valueSum";
    public static final String VALUE3_ATTRIBUTE = "attribute";
    public static final String VALUE3_VALUELIST = "valueList";
    public static final String VALUE3_RAWLIST = "rawList";
    // Others
    public static final int MAX_PROPERTY_LENGTH = 160;

    /**
     * Database Columns values Use it instead of literals
     */
    public static final String DB_TEST = "Test";
    public static final String DB_TESTCASE = "Testcase";
    public static final String DB_COUNTRY = "Country";
    public static final String DB_PROPERTY = "Property";
    public static final String DB_TYPE = "Type";
    public static final String DB_DATABASE = "Database";
    public static final String DB_VALUE1 = "Value1";
    public static final String DB_VALUE2 = "Value2";
    public static final String DB_VALUE3 = "Value3";
    public static final String DB_LENGTH = "Length";
    public static final String DB_ROWLIMIT = "RowLimit";
    public static final String DB_NATURE = "Nature";
    public static final String DB_CACHEEXPIRE = "CacheExpire";
    public static final String DB_RETRYNB = "RetryNb";
    public static final String DB_RETRYPERIOD = "RetryPeriod";
    public static final String DB_DESCRIPTION = "Description";
    public static final String DB_RANK = "Rank";
    public static final String DB_USRCREATED = "UsrCreated";
    public static final String DB_DATECREATED = "DateCreated";
    public static final String DB_USRMODIF = "UsrModif";
    public static final String DB_DATEMODIF = "DateModif";

    @JsonIgnore
    public List<TestCaseCountry> getTestcaseCountries() {
        return testcaseCountries;
    }

    @JsonIgnore
    public TestCaseCountry getTestcaseCountry() {
        return testcaseCountry;
    }

    @JsonIgnore
    public List<Invariant> getInvariantCountries() {
        return invariantCountries;
    }

    public void setInvariantCountries(List<Invariant> invariantCountries) {
        this.invariantCountries = invariantCountries;
    }

    @JsonIgnore
    public MessageEvent getResult() {
        return result;
    }

    public boolean hasSameKey(TestCaseCountryProperties obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        if (!Objects.equals(this.test, obj.test)) {
            return false;
        }
        if (!Objects.equals(this.testcase, obj.testcase)) {
            return false;
        }
        if (!Objects.equals(this.country, obj.country)) {
            return false;
        }
        return Objects.equals(this.property, obj.property);
    }

    public JSONObject toJson() {
        JSONObject testCaseCountryPropertiesJson = new JSONObject();
        try {
            testCaseCountryPropertiesJson.put("fromTest", this.getTest());
            testCaseCountryPropertiesJson.put("fromTestcase", this.getTestcase());
            testCaseCountryPropertiesJson.put("property", this.getProperty());
            testCaseCountryPropertiesJson.put("description", this.getDescription());
            testCaseCountryPropertiesJson.put("type", this.getType());
            testCaseCountryPropertiesJson.put("database", this.getDatabase());
            testCaseCountryPropertiesJson.put("value1", this.getValue1());
            testCaseCountryPropertiesJson.put("value2", this.getValue2());
            testCaseCountryPropertiesJson.put("value3", this.getValue3());
            testCaseCountryPropertiesJson.put("length", this.getLength());
            testCaseCountryPropertiesJson.put("rowLimit", this.getRowLimit());
            testCaseCountryPropertiesJson.put("retryNb", this.getRetryNb());
            testCaseCountryPropertiesJson.put("retryPeriod", this.getRetryPeriod());
            testCaseCountryPropertiesJson.put("cacheExpire", this.getCacheExpire());
            testCaseCountryPropertiesJson.put("nature", this.getNature());
            testCaseCountryPropertiesJson.put("rank", this.getRank());
            testCaseCountryPropertiesJson.put("usrCreated", this.getUsrCreated());
            testCaseCountryPropertiesJson.put("dateCreated", this.getDateCreated());
            testCaseCountryPropertiesJson.put("usrModif", this.getUsrModif());
            testCaseCountryPropertiesJson.put("dateModif", this.getDateModif());

            JSONArray countriesJson = new JSONArray();
            if (this.getInvariantCountries() != null) {
                for (Invariant countryInv : this.getInvariantCountries()) {
                    if (countryInv != null) {
                        countriesJson.put(countryInv.toJson(false));
                    }
                }
            }
            testCaseCountryPropertiesJson.put("countries", countriesJson);

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseCountryPropertiesJson;
    }

    public JSONObject toJsonV001() {
        JSONObject testCaseCountryPropertiesJson = new JSONObject();
        try {
            testCaseCountryPropertiesJson.put("JSONVersion", "001");
            testCaseCountryPropertiesJson.put("testFolder", this.getTest());
            testCaseCountryPropertiesJson.put("testcase", this.getTestcase());
            testCaseCountryPropertiesJson.put("property", this.getProperty());
            testCaseCountryPropertiesJson.put("description", this.getDescription());
            testCaseCountryPropertiesJson.put("type", this.getType());
            testCaseCountryPropertiesJson.put("database", this.getDatabase());
            testCaseCountryPropertiesJson.put("value1", this.getValue1());
            testCaseCountryPropertiesJson.put("value2", this.getValue2());
            testCaseCountryPropertiesJson.put("value3", this.getValue3());
            testCaseCountryPropertiesJson.put("length", this.getLength());
            testCaseCountryPropertiesJson.put("rowLimit", this.getRowLimit());
            testCaseCountryPropertiesJson.put("nature", this.getNature());
            testCaseCountryPropertiesJson.put("rank", this.getRank());
            testCaseCountryPropertiesJson.put("usrCreated", this.getUsrCreated());
            testCaseCountryPropertiesJson.put("dateCreated", this.getDateCreated());
            testCaseCountryPropertiesJson.put("usrModif", this.getUsrModif());
            testCaseCountryPropertiesJson.put("dateModif", this.getDateModif());

            JSONArray countriesJson = new JSONArray();
            if (this.getInvariantCountries() != null) {
                for (Invariant countryInv : this.getInvariantCountries()) {
                    if (countryInv != null) {
                        countriesJson.put(countryInv.toJsonV001());
                    }
                }
            }
            testCaseCountryPropertiesJson.put("countries", countriesJson);

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return testCaseCountryPropertiesJson;
    }

}
