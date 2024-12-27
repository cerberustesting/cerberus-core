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

import java.sql.Timestamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;



/**
 *
 * @author bdumont
 */
public class TestCaseExecutionHttpStat {
    
    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionHttpStat.class);

    
    private long id;
    private Timestamp start;
    private String controlStatus;
    private String system;
    private String application;
    private String test;
    private String testcase;
    private String country;
    private String environment;
    private String robotDecli;
    private int total_hits;
    private int total_size;
    private int total_time;
    private int internal_hits;
    private int internal_size;
    private int internal_time;
    private int img_size;
    private int img_size_max;
    private int img_hits;
    private int js_size;
    private int js_size_max;
    private int js_hits;
    private int css_size;
    private int css_size_max;
    private int css_hits;
    private int html_size;
    private int html_size_max;
    private int html_hits;
    private int media_size;
    private int media_size_max;
    private int media_hits;
    private int nb_thirdparty;
    private String crbVersion;
    private JSONObject statDetail;
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

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public String getControlStatus() {
        return controlStatus;
    }

    public void setControlStatus(String controlStatus) {
        this.controlStatus = controlStatus;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestcase() {
        return testcase;
    }

    public void setTestcase(String testcase) {
        this.testcase = testcase;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getRobotDecli() {
        return robotDecli;
    }

    public void setRobotDecli(String robot) {
        this.robotDecli = robot;
    }

    public int getTotal_hits() {
        return total_hits;
    }

    public void setTotal_hits(int total_hits) {
        this.total_hits = total_hits;
    }

    public int getTotal_size() {
        return total_size;
    }

    public void setTotal_size(int total_size) {
        this.total_size = total_size;
    }

    public int getTotal_time() {
        return total_time;
    }

    public void setTotal_time(int total_time) {
        this.total_time = total_time;
    }

    public int getInternal_hits() {
        return internal_hits;
    }

    public void setInternal_hits(int internal_hits) {
        this.internal_hits = internal_hits;
    }

    public int getInternal_size() {
        return internal_size;
    }

    public void setInternal_size(int internal_size) {
        this.internal_size = internal_size;
    }

    public int getInternal_time() {
        return internal_time;
    }

    public void setInternal_time(int internal_time) {
        this.internal_time = internal_time;
    }

    public int getImg_size() {
        return img_size;
    }

    public void setImg_size(int img_size) {
        this.img_size = img_size;
    }

    public int getImg_size_max() {
        return img_size_max;
    }

    public void setImg_size_max(int img_size_max) {
        this.img_size_max = img_size_max;
    }

    public int getImg_hits() {
        return img_hits;
    }

    public void setImg_hits(int img_hits) {
        this.img_hits = img_hits;
    }

    public int getJs_size() {
        return js_size;
    }

    public void setJs_size(int js_size) {
        this.js_size = js_size;
    }

    public int getJs_size_max() {
        return js_size_max;
    }

    public void setJs_size_max(int js_size_max) {
        this.js_size_max = js_size_max;
    }

    public int getJs_hits() {
        return js_hits;
    }

    public void setJs_hits(int js_hits) {
        this.js_hits = js_hits;
    }

    public int getCss_size() {
        return css_size;
    }

    public void setCss_size(int css_size) {
        this.css_size = css_size;
    }

    public int getCss_size_max() {
        return css_size_max;
    }

    public void setCss_size_max(int css_size_max) {
        this.css_size_max = css_size_max;
    }

    public int getCss_hits() {
        return css_hits;
    }

    public void setCss_hits(int css_hits) {
        this.css_hits = css_hits;
    }

    public int getHtml_size() {
        return html_size;
    }

    public void setHtml_size(int html_size) {
        this.html_size = html_size;
    }

    public int getHtml_size_max() {
        return html_size_max;
    }

    public void setHtml_size_max(int html_size_max) {
        this.html_size_max = html_size_max;
    }

    public int getHtml_hits() {
        return html_hits;
    }

    public void setHtml_hits(int html_hits) {
        this.html_hits = html_hits;
    }

    public int getMedia_size() {
        return media_size;
    }

    public void setMedia_size(int media_size) {
        this.media_size = media_size;
    }

    public int getMedia_size_max() {
        return media_size_max;
    }

    public void setMedia_size_max(int media_size_max) {
        this.media_size_max = media_size_max;
    }

    public int getMedia_hits() {
        return media_hits;
    }

    public void setMedia_hits(int media_hits) {
        this.media_hits = media_hits;
    }

    public int getNb_thirdparty() {
        return nb_thirdparty;
    }

    public void setNb_thirdparty(int nb_thirdparty) {
        this.nb_thirdparty = nb_thirdparty;
    }

    public String getCrbVersion() {
        return crbVersion;
    }

    public void setCrbVersion(String crbVersion) {
        this.crbVersion = crbVersion;
    }

    public JSONObject getStatDetail() {
        return statDetail;
    }

    public void setStatDetail(JSONObject statDetail) {
        this.statDetail = statDetail;
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

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", this.getId());
            result.put("test", this.getTest());
            result.put("testcase", this.getTestcase());
            result.put("environment", this.getEnvironment());
            result.put("country", this.getCountry());
            result.put("start", this.getStart());
            result.put("controlStatus", this.getControlStatus());
            result.put("application", this.getApplication());
            result.put("system", this.getSystem());
            result.put("robotDecli", this.getRobotDecli());

            result.put("totalHits", this.getTotal_hits());
            result.put("totalSize", this.getTotal_size());
            result.put("totalTime", this.getTotal_time());
            result.put("internalHits", this.getInternal_hits());
            result.put("internalSize", this.getInternal_size());
            result.put("internalTime", this.getInternal_time());

            result.put("imgSize", this.getImg_size());
            result.put("imgSizeMax", this.getImg_size_max());
            result.put("imgHits", this.getImg_hits());
            result.put("cssSize", this.getImg_size());
            result.put("cssSizeMax", this.getImg_size_max());
            result.put("cssHits", this.getImg_hits());
            result.put("htmlSize", this.getImg_size());
            result.put("htmlSizeMax", this.getImg_size_max());
            result.put("htmlHits", this.getImg_hits());
            result.put("jsSize", this.getImg_size());
            result.put("jsSizeMax", this.getImg_size_max());
            result.put("jsHits", this.getImg_hits());
            result.put("mediaSize", this.getImg_size());
            result.put("mediaSizeMax", this.getImg_size_max());
            result.put("mediaHits", this.getImg_hits());
            result.put("nbThirdParty", this.getNb_thirdparty());
            result.put("crbVersion", this.getCrbVersion());
            result.put("stat", this.getStatDetail());
            
            result.put("usrCreated", this.getUsrCreated());
            result.put("dateCreated", this.getDateCreated());
            result.put("usrModif", this.getUsrModif());
            result.put("dateModif", this.getDateModif());

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }
    
    
}
