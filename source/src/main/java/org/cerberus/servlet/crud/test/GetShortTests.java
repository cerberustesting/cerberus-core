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
package org.cerberus.servlet.crud.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.log.MyLogger;
import org.cerberus.crud.service.ITestService;
import org.cerberus.crud.service.impl.TestService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/02/2013
 * @since 2.0.0
 */
//@WebServlet(value = "/GetShortTests")
public class GetShortTests extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestService testService = appContext.getBean(TestService.class);

        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        for (String test : testService.getListOfTests()) {
            array.put(test);
        }
        try {
            jsonObject.put("testsList", array);

            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(jsonObject.toString());
        } catch (JSONException exception) {
            MyLogger.log(GetShortTests.class.getName(), Level.WARN, exception.toString());
        }
    }
}
