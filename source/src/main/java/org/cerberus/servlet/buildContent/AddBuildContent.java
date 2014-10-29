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

package org.cerberus.servlet.buildContent;

import org.apache.log4j.Logger;
import org.cerberus.entity.BuildRevisionParameters;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.IBuildRevisionParametersService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.BuildRevisionParametersService;
import org.cerberus.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AddBuildContent", urlPatterns = {"/AddBuildContent"})
public class AddBuildContent extends HttpServlet{

    private static final Logger LOG = Logger.getLogger(AddBuildContent.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IBuildRevisionParametersService buildRevisionParametersService = appContext.getBean(BuildRevisionParametersService.class);

        BuildRevisionParameters brp = new BuildRevisionParameters();
        brp.setBuild(req.getParameter("sprint"));
        brp.setRevision(req.getParameter("revision"));
        brp.setRelease(req.getParameter("release"));
        brp.setLink(req.getParameter("link"));
        brp.setApplication(req.getParameter("application"));
        brp.setReleaseOwner(req.getParameter("owner"));
        brp.setProject(req.getParameter("project"));
        brp.setBudIdFixed(req.getParameter("bug"));
        brp.setTicketIdFixed(req.getParameter("ticket"));
        brp.setSubject(req.getParameter("subject"));

        buildRevisionParametersService.insertBuildRevisionParameters(brp);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
        try {
            logEventService.insertLogEvent(factoryLogEvent.create(0, 0, req.getUserPrincipal().getName(), null, "/AddBuildContent", "CREATE", "Insert BuildContent : " + brp.getRelease(), "", ""));
        } catch (CerberusException ex) {
            LOG.warn("Unable to register log : " + ex.getMessageError().getDescription(), ex);
        }

        resp.getWriter().print(brp.getRelease());
    }
}
