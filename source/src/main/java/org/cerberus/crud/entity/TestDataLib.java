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
import java.util.List;

/**
 *
 * @author vertigo17
 */
public class TestDataLib {

    private Integer testDataLibID;
    private String name;
    private String system;
    private String environment;
    private String country;
    private String privateData;
    private String group;
    private String type;
    private String database;
    private String script;
    private String databaseUrl;
    private String service;
    private String servicePath;
    private String method;
    private String envelope;
    private String databaseCsv;
    private String csvUrl;
    private String separator;
    private String description;
    private String creator;
    private Timestamp created;
    private String lastModifier;
    private Timestamp lastModified;

    // Not included in table.
    // Master subdata record (correspond to subdata='')
    private String subDataValue;
    private String subDataColumn;
    private String subDataParsingAnswer;
    private String subDataColumnPosition;
    private List<TestDataLibData> subDataLib;

    public static final String TYPE_INTERNAL = "INTERNAL";
    public static final String TYPE_SQL = "SQL";
    public static final String TYPE_SERVICE = "SERVICE";
    public static final String TYPE_CSV = "CSV";

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<TestDataLibData> getSubDataLib() {
        return subDataLib;
    }

    public void setSubDataLib(List<TestDataLibData> subDataLib) {
        this.subDataLib = subDataLib;
    }

    public String getDatabaseCsv() {
        return databaseCsv;
    }

    public void setDatabaseCsv(String databaseCsv) {
        this.databaseCsv = databaseCsv;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Integer getTestDataLibID() {
        return testDataLibID;
    }

    public void setTestDataLibID(Integer testDataLibID) {
        this.testDataLibID = testDataLibID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPrivateData() {
        return privateData;
    }

    public void setPrivateData(String privateData) {
        this.privateData = privateData;
    }
    
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp Created) {
        this.created = Created;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp LastModified) {
        this.lastModified = LastModified;
    }

    public String getSubDataValue() {
        return subDataValue;
    }

    public void setSubDataValue(String subDataValue) {
        this.subDataValue = subDataValue;
    }

    public String getSubDataColumn() {
        return subDataColumn;
    }

    public void setSubDataColumn(String subDataColumn) {
        this.subDataColumn = subDataColumn;
    }

    public String getSubDataParsingAnswer() {
        return subDataParsingAnswer;
    }

    public void setSubDataParsingAnswer(String subDataParsingAnswer) {
        this.subDataParsingAnswer = subDataParsingAnswer;
    }

    public String getSubDataColumnPosition() {
        return subDataColumnPosition;
    }

    public void setSubDataColumnPosition(String subDataColumnPosition) {
        this.subDataColumnPosition = subDataColumnPosition;
    }

    public String getCsvUrl() {
        return csvUrl;
    }

    public void setCsvUrl(String csvUrl) {
        this.csvUrl = csvUrl;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

}
