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
package org.cerberus.util.security;

import org.apache.commons.lang3.*;
import org.apache.commons.text.StringEscapeUtils;
import org.cerberus.crud.entity.UserSystem;
import org.springframework.web.context.request.*;

import javax.servlet.http.*;
import java.util.*;
import java.util.stream.*;

public class UserSecurity {

    public static boolean systemIsAllow(String system) {
        return UserSecurity.systemIsAllow(Stream.of(system).collect(Collectors.toList()));
    }

    public static boolean systemIsAllow(List<String> systems) {
        List<String> systemsAllow = UserSecurity.getSystemAllow();

        for(String system : systems) {
            if( ! systemsAllow.contains(system) ) return false;
        }

        return true;

    }


    /**
     * Return all system allow for the current user
     * @return
     */
    public static List<String> getSystemAllow() {
        // RG, if it is an administrator, he can access to all system
        if(getCurrentHttpRequest().isUserInRole("Administrator")) {
            return null;
        }


        List<UserSystem> userSystemList = (List<UserSystem>) getSession().getAttribute("MySystemsAllow");

        List<String> res = new LinkedList<>();
        for(UserSystem systemUser : userSystemList) {
            res.add(systemUser.getSystem());
        }

        return res;
    }


    public static String getSystemAllowForSQLInClause() {
        StringBuilder st = new StringBuilder();
        boolean firstSys = true;
        for (String sys : getSystemAllow()) {
            st.append(  (!firstSys ? "," : "")  + "'" + StringEscapeUtils.escapeHtml4(escapeSql(sys)) + "'");
            firstSys = false;
        }
        return "(" + st.toString() + ")";
    }


    public static boolean isAdministrator() {
        return getCurrentHttpRequest().isUserInRole("Administrator");
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    private static HttpServletRequest getCurrentHttpRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            return request;
        }
        return null;
    }

    private static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return StringUtils.replace(str, "'", "''");
    }
}
