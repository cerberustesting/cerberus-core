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
package org.cerberus.servlet.campaign;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IInvariantService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "ListCampaignParameter", urlPatterns = {"/ListCampaignParameter"})
public class ListCampaignParameter extends HttpServlet {

    private IInvariantService invariantService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        invariantService = appContext.getBean(IInvariantService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String invariant = policy.sanitize(request.getParameter("invariant"));

        JSONObject jsonResponse = new JSONObject();
        try {
            if (invariant == null || "".equals(invariant.trim())) {
                List<Invariant> campagneParametersInvariantList = invariantService.findListOfInvariantById("CAMPAIGN_PARAMETER");
                if (campagneParametersInvariantList != null && campagneParametersInvariantList.size() > 0) {
                    jsonResponse.put("CampaignsParameters", convertInvariantsListToJsonArray(campagneParametersInvariantList));
                }
            } else {
                List<Invariant> parameterValueList = invariantService.findListOfInvariantById(invariant);
                if (parameterValueList != null && parameterValueList.size() > 0) {
                    jsonResponse.put("ParameterValues", convertInvariantsListToJsonArray(parameterValueList));
                }
            }
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (CerberusException ex) {
            response.setContentType("text/html");
            response.getWriter().print(ex.getMessageError().getDescription());
        } catch (JSONException ex) {
            response.setContentType("text/html");
            response.getWriter().print(ex.getMessage());
        }
    }

    private JSONArray convertInvariantsListToJsonArray(List<Invariant> invariantsList) throws JSONException, CerberusException {
        JSONArray jsonResponse = new JSONArray();
        for (Invariant invariant : invariantsList) {
            jsonResponse.put(convertInvariantToJson(invariant));
        }

        return jsonResponse;
    }

    private JSONArray convertInvariantToJson(Invariant invariant) throws JSONException {
        JSONArray result = new JSONArray();
        result.put(invariant.getIdName());
        result.put(invariant.getValue());
        result.put(invariant.getGp1());
        result.put(invariant.getDescription());
        result.put(invariant.getVeryShortDesc());
        return result;
    }
}
