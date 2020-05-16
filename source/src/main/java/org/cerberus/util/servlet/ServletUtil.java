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
package org.cerberus.util.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author vertigo
 */
public final class ServletUtil {

    private static final Logger LOG = LogManager.getLogger(ServletUtil.class);

    private static final long DEFAULT_WAIT_MS = 20;

    private ServletUtil() {
    }

    /**
     * This method should be called in every servlet call. That allow
     * transversal action to be performed in debug mode. Actions can be for
     * example to delay every called to simulate a slow response time and
     * validate the behaviour of the GUI.
     *
     * @param request
     */
    public static void servletStart(HttpServletRequest request) {

        if (LOG.isDebugEnabled()) {

            // Wait in order to simulate some slow response time.
            long timeToWait = DEFAULT_WAIT_MS;
            try {
                switch (request.getServletPath()) {
                    case "/ReadCampaign":
                        timeToWait = 10;
                        break;
                    case "/ReadTestCaseExecutionByTag":
                        timeToWait = 30;
                        break;
                    case "/ReadExecutionStat":
                        timeToWait = 30;
                        break;
                    default:
                }
                LOG.debug("Servlet " + request.getServletPath() + " - Waiting : " + timeToWait);
                LOG.debug("Servlet Query String " + request.getQueryString());
                Thread.sleep(timeToWait);

            } catch (InterruptedException ex) {
                LOG.error(ex, ex);
            }
        }

    }

    public static String getUser(HttpServletRequest request) {
        String user = request.getRemoteUser();
        if (user != null) {
            return user;
        }
        if (request.getUserPrincipal() != null) {
            user = request.getUserPrincipal().getName();
            if (user == null) {
                return "";
            } else {
                return user;
            }
        }
        return "";
    }

}
