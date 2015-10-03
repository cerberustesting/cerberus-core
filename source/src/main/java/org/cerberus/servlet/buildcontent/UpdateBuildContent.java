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
package org.cerberus.servlet.buildcontent;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.BuildRevisionParameters;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryLogEvent;
import org.cerberus.crud.factory.impl.FactoryLogEvent;
import org.cerberus.crud.service.IBuildRevisionParametersService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.impl.BuildRevisionParametersService;
import org.cerberus.crud.service.impl.LogEventService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "UpdateBuildContent", urlPatterns = {"/UpdateBuildContent"})
public class UpdateBuildContent extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UpdateBuildContent.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        int columnPosition = Integer.parseInt(req.getParameter("columnPosition"));
        String value = req.getParameter("value").replaceAll("'", "");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IBuildRevisionParametersService buildRevisionParametersService = appContext.getBean(BuildRevisionParametersService.class);

        BuildRevisionParameters brp = buildRevisionParametersService.findBuildRevisionParametersByKey(id);

        switch (columnPosition) {
            case 0:
                brp.setBuild(value);
                break;
            case 1:
                brp.setRevision(value);
                break;
            case 2:
                brp.setApplication(value);
                break;
            case 3:
                brp.setRelease(value);
                break;
            case 4:
                brp.setProject(value);
                break;
            case 5:
                brp.setTicketIdFixed(value);
                break;
            case 6:
                brp.setBugIdFixed(value);
                break;
            case 7:
                brp.setSubject(value);
                break;
            case 8:
                brp.setReleaseOwner(value);
                break;
            case 9:
                brp.setLink(value);
                break;
        }

        buildRevisionParametersService.updateBuildRevisionParameters(brp);

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createPrivateCalls("/UpdateBuildContent", "UPDATE", "Updated BuildContent : " + brp.getRelease(), req);

        resp.getWriter().print(value);
    }
}
