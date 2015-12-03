/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.util;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author memiks
 */
public class SqlUtil {

    /**
     *
     * @param obj List of generic object that have a toString Method
     * implementation
     * @return a String that has all obj.toString values separated by , and
     * surounded by '
     */
    public static String getInSQLClause(List<?> obj) {
        if (obj == null) {
            return "";
        }
        if (obj.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("in (");
        for (Object myObj : obj) {
            result.append("'");
            result.append(myObj.toString());
            result.append("',");
        }
        String res = result.toString().substring(0, (result.length() - 1));
        return res + ")";
    }

    public static String createWhereInClause(String field, List<String> values, boolean isString) {

        if (field == null || field.isEmpty() || values == null || values.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder(field).append(" IN (");
        int index = 0;

        String separator;

        if (isString) {
            separator = "', '";
            stringBuilder.append("'");
        } else {
            separator = ", ";
        }

        for (String value : values) {
            if (value != null) {
                if (index > 0) {
                    stringBuilder.append(separator);
                }
                stringBuilder.append(value);
                index++;
            }
        }

        if (isString) {
            stringBuilder.append("')");
        } else {
            stringBuilder.append(")");
        }

        return stringBuilder.toString();
    }
    /**
     * Generates an IN (?, ?) clause. The IN clause uses the ? wildcard to represent each parameter included in the IN's set.
     * @param field
     * @param list
     * @return 
     */
     public static String generateInClause(String field, List<String> list){
        StringBuilder clause = new StringBuilder();
        if(list != null && list.size() > 0){
            clause.append(field).append( " in (");            
            clause.append(StringUtils.repeat("?, ", list.size()));
            clause.append(") ");
        }
        
        return clause.toString().replace("?, )", "?)");
    }
}
