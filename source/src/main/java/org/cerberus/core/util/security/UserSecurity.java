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
package org.cerberus.core.util.security;

import org.apache.commons.lang3.*;
import org.apache.commons.text.StringEscapeUtils;
import org.cerberus.core.crud.entity.UserSystem;
import org.springframework.web.context.request.*;

import javax.servlet.http.*;
import java.util.*;
import java.util.stream.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserSecurity {

    private static final Logger LOG = LogManager.getLogger(UserSecurity.class);

    public static boolean systemIsAllow(String system) {
        return UserSecurity.systemIsAllow(Stream.of(system).collect(Collectors.toList()));
    }

    public static boolean systemIsAllow(List<String> systems) {
        List<String> systemsAllow = UserSecurity.getSystemAllow();

        for (String system : systems) {
            if (!systemsAllow.contains(system)) {
                return false;
            }
        }

        return true;

    }

    /**
     * Return all system allow for the current user
     *
     * @return
     */
    public static List<String> getSystemAllow() {
        LOG.debug("Get Allowed system for : " + getCurrentHttpRequest().getRemoteUser());

        /**
         * RG. If it is an administrator, he can access to all system. Note that
         * this does not work when http call is made on public servlets.
         */
        // 
        if (getCurrentHttpRequest().isUserInRole("Administrator")) {
            LOG.debug("Administrator user : " + getCurrentHttpRequest().getRemoteUser());
            return null;
        }

        @SuppressWarnings("unchecked")
        List<UserSystem> userSystemList = (List<UserSystem>) getSession().getAttribute("MySystemsAllow");

        /**
         * If systemAllow is null, request comes from any public servlet
         * (RunTestCaseVXXX, AddToExecutionQueueVXXX,...) without
         * authenticitation done => authorize all system in this case
         */
        if (userSystemList == null) {
            return null;
        }

        /**
         * If request comes from any public servlet (RunTestCaseVXXX,
         * AddToExecutionQueueVXXX,...) WITH authenticitation done, there is no
         * other way to get the information that user is Administrator than
         * getting is from Session Attribute
         */
        if ((boolean) getSession().getAttribute("MySystemsIsAdministrator")) {
            return null;
        }

        List<String> res = new LinkedList<>();
        for (UserSystem systemUser : userSystemList) {
            res.add(systemUser.getSystem());
        }

        return res;
    }

    public static String getSystemAllowForSQL(String systemAttributeName) {
        StringBuilder st = new StringBuilder();
        boolean firstSys = true;

        List<String> systemAllow = getSystemAllow();
        LOG.debug("Allowed system : " + systemAllow);
        if (systemAllow == null) {
            return " 1=1 ";
        }

        for (String sys : getSystemAllow()) {
            st.append(!firstSys ? "," : "").append("'").append(StringEscapeUtils.escapeHtml4(escapeSql(sys))).append("'");
            firstSys = false;
        }
        return systemAttributeName + " in (''," + st.toString() + ")";
    }

    public static boolean isAdministrator() {
        return getCurrentHttpRequest().isUserInRole("Administrator");
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    private static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
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

    private UserSecurity() {
    }
}
