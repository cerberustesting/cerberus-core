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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.cerberus.entity.Campaign;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.ICampaignService;
import org.cerberus.servlet.invariant.GetInvariantList;
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
@WebServlet(name = "GetCampaign", urlPatterns = {"/GetCampaign"})
public class GetCampaign extends HttpServlet {

    private ICampaignService campaignService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        campaignService = appContext.getBean(ICampaignService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String action = policy.sanitize(request.getParameter("action"));

        try {
            JSONObject jsonResponse = new JSONObject();
            try {
                if (action != null && "findAllCampaign".equals(action.trim())) {
                    jsonResponse = findAllCampaignToJSON();
                }
            } catch (CerberusException ex) {
                response.setContentType("text/html");
                response.getWriter().print(ex.getMessageError().getDescription());

            }
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            MyLogger.log(GetInvariantList.class.getName(), Level.FATAL, "" + e);
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
        }
    }

    private JSONObject findAllCampaignToJSON() throws JSONException, CerberusException {
        JSONObject jsonResponse = new JSONObject();
        for (Campaign campaign : campaignService.findAll()) {
            jsonResponse.put(campaign.getCampaign(), convertCampaignToJSONObject(campaign));
        }

        return jsonResponse;
    }

    private JSONObject convertCampaignToJSONObject(Campaign campaign) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("CampaignID", campaign.getCampaignID());
        result.put("Campaign", campaign.getCampaign());
        result.put("Description", campaign.getDescription());
        return result;
    }
}
