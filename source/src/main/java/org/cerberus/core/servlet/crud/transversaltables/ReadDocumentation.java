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
package org.cerberus.core.servlet.crud.transversaltables;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cerberus.core.crud.entity.Documentation;
import org.cerberus.core.crud.service.IDocumentationService;
import org.cerberus.core.util.ParameterParserUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebServlet(name = "ReadDocumentation", urlPatterns = {"/ReadDocumentation"})
public class ReadDocumentation extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadDocumentation.class);
    
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        JSONObject jsonResponse = new JSONObject();
        List<Documentation> result = new ArrayList<>();
        JSONObject format = new JSONObject();

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        String lang = ParameterParserUtil.parseStringParamAndSanitize(httpServletRequest.getParameter("lang"), "en");

        result = docService.findAllWithEmptyDocLabel(lang);
        format = docService.formatGroupByDocTable(result);
        try {
            jsonResponse.put("labelTable", format);
        } catch (JSONException ex) {
            LOG.warn(ex);
        }
        response.getWriter().print(jsonResponse.toString());
    }

    private JSONObject convertDocToJSONObject(Documentation doc) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(doc));
        return result;
    }
}
