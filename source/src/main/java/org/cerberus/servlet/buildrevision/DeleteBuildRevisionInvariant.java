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
package org.cerberus.servlet.buildrevision;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.entity.BuildRevisionInvariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryBuildRevisionInvariant;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryBuildRevisionInvariant;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.IBuildRevisionInvariantService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.BuildRevisionInvariantService;
import org.cerberus.service.impl.LogEventService;
import org.cerberus.service.impl.UserService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "DeleteBuildRevisionInvariant", urlPatterns = {"/DeleteBuildRevisionInvariant"})
public class DeleteBuildRevisionInvariant extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String system = policy.sanitize(request.getParameter("system"));
        Integer level = Integer.valueOf(policy.sanitize(request.getParameter("level")));
        Integer seq = Integer.valueOf(policy.sanitize(request.getParameter("seq")));

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IBuildRevisionInvariantService briService = appContext.getBean(BuildRevisionInvariantService.class);
        IFactoryBuildRevisionInvariant factoryBuildRevisionInvariant = new FactoryBuildRevisionInvariant();

        BuildRevisionInvariant myBRI = factoryBuildRevisionInvariant.create(system, level, seq, "");
        if (briService.deleteBuildRevisionInvariant(myBRI)) {

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/DeleteBuildRevisionInvariant", "DELETE", "Delete buildRevisionInvariant : " + system + "-" + level + "-" + seq, "", ""));
            } catch (CerberusException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
            }

        } else {
            response.getWriter().print("Could not Delete Build Revision : " + system + "-" + level + "-" + seq);

        }


    }
}