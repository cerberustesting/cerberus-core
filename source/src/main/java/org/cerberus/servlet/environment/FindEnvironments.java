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

package org.cerberus.servlet.environment;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.CountryEnvParam;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ICountryEnvParamService;
import org.cerberus.crud.service.IInvariantService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "FindEnvironments", urlPatterns = {"/FindEnvironments"})
public class FindEnvironments extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(FindEnvironments.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String echo = req.getParameter("sEcho");

        String system = req.getParameter("system");
        String country = req.getParameter("Country").equalsIgnoreCase("ALL") ? "" : req.getParameter("Country");
        String envActive = req.getParameter("EnvActive").equalsIgnoreCase("ALL") ? "" : req.getParameter("EnvActive");
        String envType = req.getParameter("EnvType").equalsIgnoreCase("ALL") ? "" : req.getParameter("EnvType");
        String build = req.getParameter("build").equalsIgnoreCase("ALL") ? "" : req.getParameter("build");
        String chain = req.getParameter("chain").equalsIgnoreCase("ALL") ? "" : req.getParameter("chain");
        String envGroup = req.getParameter("envGroup").equalsIgnoreCase("ALL") ? "" : req.getParameter("envGroup");
        String environment = req.getParameter("environment").equalsIgnoreCase("ALL") ? "" : req.getParameter("environment");
        String revision = req.getParameter("revision").equalsIgnoreCase("ALL") ? "" : req.getParameter("revision");


        CountryEnvParam input = new CountryEnvParam();
        input.setSystem(system);
        input.setCountry(country);
        if (envActive.equalsIgnoreCase("Y")) {
            input.setActive(true);
        } else {
            input.setActive(false);
        }
        input.setType(envType);
        input.setBuild(build);
        input.setChain(chain);
        input.setEnvironment(environment);
        input.setRevision(revision);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);
        IInvariantService invariantService = appContext.getBean(IInvariantService.class);

        JSONObject jsonResponse = new JSONObject();
        try {
            Map<String, String> mapEnvGroup = new HashMap<String, String>();
            for (Invariant inv : invariantService.findListOfInvariantById("ENVIRONMENT")) {
                mapEnvGroup.put(inv.getValue(), inv.getGp1());
            }

            JSONArray array = new JSONArray();
            for (CountryEnvParam cep : countryEnvParamService.findCountryEnvParamByCriteria(input)) {

                if (envGroup.equalsIgnoreCase("") || envGroup.equalsIgnoreCase(mapEnvGroup.get(cep.getEnvironment()))) {
                    JSONArray data = new JSONArray();
                    data.put(cep.getSystem());
                    data.put(cep.getCountry());
                    data.put(cep.getEnvironment());
                    data.put(cep.getBuild());
                    data.put(cep.getRevision());
                    data.put(cep.getChain());
                    if (cep.isActive()) {
                        data.put("Y");
                    } else {
                        data.put("N");
                    }
                    data.put(cep.getType());

                    StringBuilder sb = new StringBuilder("Environment.jsp?system=");
                    sb.append(cep.getSystem());
                    sb.append("&country=");
                    sb.append(cep.getCountry());
                    sb.append("&env=");
                    sb.append(cep.getEnvironment());

                    data.put(sb.toString());

                    array.put(data);
                }
            }

            jsonResponse.put("aaData", array);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", array.length());
            jsonResponse.put("iTotalDisplayRecords", array.length());
            resp.setContentType("application/json");
            resp.getWriter().print(jsonResponse.toString());
        } catch (CerberusException e) {
            LOG.error("Unable to find CountryEnvParam: " + e.getMessageError().getDescription());
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessage());
        } catch (JSONException e) {
            LOG.error("Unable to convert data to JSON : " + e.getMessage());
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessage());
        }
    }
}
