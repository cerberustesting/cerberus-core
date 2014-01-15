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
package org.cerberus.servlet.application;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.entity.Application;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IApplicationService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 */
@WebServlet(name = "GetApplicationList", urlPatterns = {"/GetApplicationList"})
public class GetApplicationList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IApplicationService applicationService = appContext.getBean(IApplicationService.class);

        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            for (Application myAppli : applicationService.findAllApplication()) {
                array.put(myAppli.getApplication());
            }
            try {
                jsonObject.put("parameterList", array);

                httpServletResponse.setContentType("application/json");
                httpServletResponse.getWriter().print(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (CerberusException ex) {
            Logger.getLogger(GetApplicationList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
