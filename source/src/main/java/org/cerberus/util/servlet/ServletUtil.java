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
package org.cerberus.util.servlet;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author vertigo
 */
public final class ServletUtil {

    private static final Logger LOG = Logger.getLogger(ServletUtil.class);

    private static final long DEFAULT_WAIT_MS = 2000;

    /**
     * This method should be called in every servlet call. That allow transversal action to be performed in debug mode.
     * Actions can be for example to delay every called to simulate a slow response time and validate the behaviour of the GUI.
     * @param request
     */
    public static void servletStart(HttpServletRequest request) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Servlet Waiting : " + DEFAULT_WAIT_MS);
            try {
                Thread.sleep(DEFAULT_WAIT_MS);
            } catch (InterruptedException ex) {
                LOG.error(ex);
            }
        }

    }

}
