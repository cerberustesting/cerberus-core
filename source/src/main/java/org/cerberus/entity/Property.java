/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.entity;

import java.util.List;

import org.cerberus.util.StringUtil;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class Property {

    public static final String NATURE_STATIC = "STATIC";
    public static final String NATURE_RANDOM = "RANDOM";
    public static final String NATURE_RANDOMNEW = "RANDOMNEW";
    public static final String NATURE_NOTINUSE = "NOTINUSE";
    public static final String TYPE_SQL = "SQL";
    public static final String TYPE_LIBSQL = "LIB_SQL";
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_HTML = "HTML";
    public static final String TYPE_JS = "JS";

    /**
     * Name of property.
     */
    private String name;
    /**
     * Country used for property.
     */
    private Country country;
    /**
     * Type of property (SQL, TEXT, HTML, SQL_LIB).
     */
    private String type;
    /**
     * Database used for SQL type of property.
     */
    private String database;
    /**
     * Value of property (SQL to run, id of HTML, etc).
     */
    private String value;
    /**
     *
     */
    private int length;
    /**
     *
     */
    private int rowLimit;
    /**
     *
     */
    private String nature;
    /**
     *
     */
    private String calculatedValue;
    /**
     *
     */
    private boolean calculated;

    private List<String> countryList;

    public List<String> getCountryList() {
        return countryList;
    }

    public void setCountryList(List<String> countryList) {
        this.countryList = countryList;
    }


    private MessageEvent messageResult;
    private long start;
    private long end;

    public Property() {
    }

    public Property(String name, Country country, String type, String database, String value, int length, int rowLimit, String nature) {
        this.name = name;
        this.country = country;
        this.type = type;
        this.database = database;
        this.value = value;
        this.length = length;
        this.rowLimit = rowLimit;
        this.nature = nature;
        this.calculatedValue = StringUtil.NULL;
        this.calculated = false;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String tempName) {
        this.name = tempName;
    }

    public Country getCountry() {
        return this.country;
    }

    public void setCountry(Country tempCountry) {
        this.country = tempCountry;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String tempType) {
        this.type = tempType;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setDatabase(String tempDatabase) {
        this.database = tempDatabase;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String tempValues) {
        this.value = tempValues;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int tempLength) {
        this.length = tempLength;
    }

    public int getRowLimit() {
        return this.rowLimit;
    }

    public void setRowLimit(int tempRowLimit) {
        this.rowLimit = tempRowLimit;
    }

    public String getNature() {
        return this.nature;
    }

    public void setNature(String tempNature) {
        this.nature = tempNature;
    }

    public String getCalculatedValue() {
        return this.calculatedValue;
    }

    public void setCalculatedValue(String tempCalculatedValue) {
        this.calculatedValue = tempCalculatedValue;
        this.calculated = true;
    }

    public boolean isCalculated() {
        return this.calculated;
    }

    public MessageEvent getMessageResult() {
        return this.messageResult;
    }

    public void setMessageResult(MessageEvent messageResult) {
        this.messageResult = messageResult;
    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

}
