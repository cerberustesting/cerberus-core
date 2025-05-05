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
package org.cerberus.core.util.servlet;

import java.util.Enumeration;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

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
                    case "/ReadAppService":
                        timeToWait = 50;
                        break;
                    case "/ReadTestCaseExecutionByTag":
                        timeToWait = 30;
                        break;
                    case "/ReadExecutionStat":
                        timeToWait = 30;
                        break;
                    case "/ReadApplication":
                        timeToWait = 30;
                        break;
                    case "/UpdateAppService":
                        timeToWait = 10;
                        break;
                    case "/ReadTag":
                        timeToWait = 50;
                        break;
                    case "/api":
                        timeToWait = 30;
                        break;
                    default:
                }
                LOG.debug("Servlet [" + request.getMethod() + "]" + request.getRequestURI() + " - Waiting : " + timeToWait);
                LOG.debug("Servlet Query String " + request.getQueryString());
                final Enumeration<String> headerS = request.getHeaderNames();
                if (headerS != null) {
                    while (headerS.hasMoreElements()) {
                        String h = headerS.nextElement();
//                        LOG.debug("Header : {} - {}", h, request.getHeader(h));
                    }
                }
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
