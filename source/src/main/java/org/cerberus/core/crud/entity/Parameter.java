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

import java.time.LocalDateTime;

/**
 *
 * @author bcivel
 */
public class Parameter {

    private String system;
    private String param;
    private String value;
    private String description;

    /**
     * From here are data outside database model.
     */
    private String system1;
    private String system1value;
    private LocalDateTime cacheEntryCreation;

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String VALUE_queueexecution_global_threadpoolsize = "cerberus_queueexecution_global_threadpoolsize";
    public static final String VALUE_queueexecution_global_threadpoolsize_master = "cerberus_queueexecution_global_threadpoolsize_master";
    public static final String VALUE_cerberus_applicationobject_path = "cerberus_applicationobject_path";
    public static final String VALUE_cerberus_exeautomedia_path = "cerberus_exeautomedia_path";
    public static final String VALUE_cerberus_exemanualmedia_path = "cerberus_exemanualmedia_path";
    public static final String VALUE_cerberus_ftpfile_path = "cerberus_ftpfile_path";
    public static final String VALUE_cerberus_testdatalibfile_path = "cerberus_testdatalibfile_path";
    public static final String VALUE_cerberus_url = "cerberus_url";
    public static final String VALUE_cerberus_gui_url = "cerberus_gui_url";
    public static final String VALUE_cerberus_screenshot_max_size = "cerberus_screenshot_max_size";
    public static final String VALUE_cerberus_smtp_host = "cerberus_smtp_host";
    public static final String VALUE_cerberus_smtp_port = "cerberus_smtp_port";
    public static final String VALUE_cerberus_smtp_username = "cerberus_smtp_username";
    public static final String VALUE_cerberus_smtp_password = "cerberus_smtp_password";
    public static final String VALUE_cerberus_smtp_isSetTls = "cerberus_smtp_isSetTls";
    public static final String VALUE_cerberus_queueexecution_enable = "cerberus_queueexecution_enable";
    public static final String VALUE_cerberus_splashpage_enable = "cerberus_splashpage_enable";
    public static final String VALUE_cerberus_apikey_enable = "cerberus_apikey_enable";
    public static final String VALUE_cerberus_apikey_value1 = "cerberus_apikey_value1";
    public static final String VALUE_cerberus_apikey_value2 = "cerberus_apikey_value2";
    public static final String VALUE_cerberus_apikey_value3 = "cerberus_apikey_value3";
    public static final String VALUE_cerberus_apikey_value4 = "cerberus_apikey_value4";
    public static final String VALUE_cerberus_apikey_value5 = "cerberus_apikey_value5";
//    public static final String VALUE_cerberus_accesskey_value = "cerberus_manage_token";
    public static final String VALUE_cerberus_manage_timeout = "cerberus_manage_timeout";
    public static final String VALUE_cerberus_executeCerberusCommand_password = "cerberus_executeCerberusCommand_password";
    public static final String VALUE_cerberus_executeCerberusCommand_path = "cerberus_executeCerberusCommand_path";
    public static final String VALUE_cerberus_executeCerberusCommand_user = "cerberus_executeCerberusCommand_user";
    public static final String VALUE_cerberus_messageinfo_text = "cerberus_messageinfo_text";
    public static final String VALUE_cerberus_messageinfo_enable = "cerberus_messageinfo_enable";
    public static final String VALUE_cerberus_webperf_thirdpartyfilepath = "cerberus_webperf_thirdpartyfilepath";
    public static final String VALUE_cerberus_creditlimit_nbexeperday = "cerberus_creditlimit_nbexeperday";
    public static final String VALUE_cerberus_creditlimit_secondexeperday = "cerberus_creditlimit_secondexeperday";

    public static final String VALUE_cerberus_xraycloud_clientsecret = "cerberus_xraycloud_clientsecret";
    public static final String VALUE_cerberus_xraycloud_clientid = "cerberus_xraycloud_clientid";
    public static final String VALUE_cerberus_xraydc_url = "cerberus_xraydc_url";
    public static final String VALUE_cerberus_xraydc_token = "cerberus_xraydc_token";
    public static final String VALUE_cerberus_xray_tokencache_duration = "cerberus_xray_tokencache_duration";
    public static final String VALUE_cerberus_xray_sendenvironments_enable = "cerberus_xray_sendenvironments_enable";
    public static final String VALUE_cerberus_sikuli_typeDelay = "cerberus_sikuli_typeDelay";
    public static final String VALUE_cerberus_testcaseautofeed_enable = "cerberus_testcaseautofeed_enable";
    public static final String VALUE_cerberus_instancelogo_url = "cerberus_instancelogo_url";
    public static final String VALUE_cerberus_pdfcampaignreportdisplaycountry_boolean = "cerberus_pdfcampaignreportdisplaycountry_boolean";
    public static final String VALUE_cerberus_pdfcampaignreportdisplayciresult_boolean = "cerberus_pdfcampaignreportdisplayciresult_boolean";
    public static final String VALUE_cerberus_reportbytag_nblinestotriggerautohide_int = "cerberus_reportbytag_nblinestotriggerautohide_int";
    public static final String VALUE_cerberus_jiracloud_url = "cerberus_jiracloud_url";
    public static final String VALUE_cerberus_jiradc_url = "cerberus_jiradc_url";
    public static final String VALUE_cerberus_jiracloud_apiuser = "cerberus_jiracloud_apiuser";
    public static final String VALUE_cerberus_jiracloud_apiuser_apitoken = "cerberus_jiracloud_apiuser_apitoken";
    public static final String VALUE_cerberus_autobugcreation_enable = "cerberus_autobugcreation_enable";
    public static final String VALUE_cerberus_github_apitoken = "cerberus_github_apitoken";
    public static final String VALUE_cerberus_azuredevops_accesstoken = "cerberus_azuredevops_accesstoken";
    public static final String VALUE_cerberus_gitlab_apitoken = "cerberus_gitlab_apitoken";
    public static final String VALUE_cerberus_use_w3c_capabilities = "cerberus_use_w3c_capabilities";

    public static final String VALUE_cerberus_tagcombofilterpersystem_boolean = "cerberus_tagcombofilterpersystem_boolean";

    public static final String VALUE_cerberus_automatescore_changehorizon = "cerberus_automatescore_changehorizon";

    public static final String SECUREDPARAMINSQLCLAUSE = "(\"cerberus_accountcreation_defaultpassword\",\"cerberus_proxyauthentification_password\",\"cerberus_jenkinsadmin_password\","
            + "\"cerberus_smtp_password\",\"cerberus_executeCerberusCommand_password\",\"cerberus_xraycloud_clientsecret\",\"cerberus_xraycloud_clientid\",\"cerberus_xraydc_token\","
            + "\"cerberus_jiracloud_apiuser_apitoken\",\"cerberus_github_apitoken\",\"cerberus_azuredevops_accesstoken\",\"cerberus_gitlab_apitoken\")";

    public static final Integer CACHE_DURATION = 60;
    public static final Integer SHORT_CACHE_DURATION = 2;

    public LocalDateTime getCacheEntryCreation() {
        return cacheEntryCreation;
    }

    public void setCacheEntryCreation(LocalDateTime cacheEntryCreation) {
        this.cacheEntryCreation = cacheEntryCreation;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getSystem1() {
        return system1;
    }

    public void setSystem1(String system) {
        this.system1 = system;
    }

    public String getSystem1value() {
        return system1value;
    }

    public void setSystem1value(String system) {
        this.system1value = system;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
