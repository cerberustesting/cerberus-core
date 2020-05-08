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
    public static final String VALUE_cerberus_testdatalibcsv_path = "cerberus_testdatalibcsv_path";
    public static final String VALUE_cerberus_url = "cerberus_url";
    public static final String VALUE_cerberus_gui_url = "cerberus_gui_url";
    public static final String VALUE_cerberus_screenshot_max_size = "cerberus_screenshot_max_size";
    public static final String VALUE_cerberus_smtp_host = "cerberus_smtp_host";
    public static final String VALUE_cerberus_smtp_port = "cerberus_smtp_port";
    public static final String VALUE_cerberus_smtp_username = "cerberus_smtp_username";
    public static final String VALUE_cerberus_smtp_password = "cerberus_smtp_password";
    public static final String VALUE_cerberus_smtp_isSetTls = "cerberus_smtp_isSetTls";
    public static final String VALUE_cerberus_queueexecution_enable = "cerberus_queueexecution_enable";
    public static final String VALUE_cerberus_manage_token = "cerberus_manage_token";
    public static final String VALUE_cerberus_manage_timeout = "cerberus_manage_timeout";
    public static final String VALUE_cerberus_executeCerberusCommand_password = "cerberus_executeCerberusCommand_password";
    public static final String VALUE_cerberus_executeCerberusCommand_path = "cerberus_executeCerberusCommand_path";
    public static final String VALUE_cerberus_executeCerberusCommand_user = "cerberus_executeCerberusCommand_user";

    public static final String SECUREDPARAMINSQLCLAUSE = "(\"cerberus_accountcreation_defaultpassword\",\"cerberus_proxyauthentification_password\",\"cerberus_jenkinsadmin_password\",\"cerberus_smtp_password\",\"cerberus_executeCerberusCommand_password\")";

    public static final Integer CACHE_DURATION = 60;

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
