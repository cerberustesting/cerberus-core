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
package org.cerberus.core.session;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
public class SessionCounterListener implements HttpSessionListener {

    @Autowired
    SessionCounter cs;

    @Override
    public void sessionCreated(HttpSessionEvent arg0) {
        WebApplicationContextUtils
                .getRequiredWebApplicationContext(arg0.getSession().getServletContext())
                .getAutowireCapableBeanFactory()
                .autowireBean(this);
        cs.identificateUser(arg0.getSession().getId(), arg0.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent arg0) {
        String key = arg0.getSession().getId();
        cs.destroyUser(key);
    }
}
