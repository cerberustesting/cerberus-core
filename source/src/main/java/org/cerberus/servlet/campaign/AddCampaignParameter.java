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
import org.apache.log4j.Logger;
import org.cerberus.entity.Campaign;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryCampaignParameter;
import org.cerberus.factory.IFactoryLogEvent;
import org.cerberus.factory.impl.FactoryLogEvent;
import org.cerberus.service.ICampaignService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.impl.LogEventService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "AddCampaignParameter", urlPatterns = {"/AddCampaignParameter"})
public class AddCampaignParameter extends HttpServlet {

    private ICampaignService campaignService;
    private IFactoryCampaignParameter factoryCampaignParameter;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);

        campaignService = appContext.getBean(ICampaignService.class);
        factoryCampaignParameter = appContext.getBean(IFactoryCampaignParameter.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String campaignId = policy.sanitize(request.getParameter("Campaign"));
        String parameter = policy.sanitize(request.getParameter("Parameter"));
        String value = policy.sanitize(request.getParameter("Value"));

        Campaign campaign;
        try {
            campaign = campaignService.findCampaignByKey(Integer.parseInt(campaignId));
            campaignService.createCampaignParameter(factoryCampaignParameter.create(null, campaign.getCampaign(), parameter, value));
            logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/AddCampaignParameter", "CREATE", "Create Campaign Parameter : " + campaignId + "/" + parameter + "/" + value, "", ""));
        } catch (CerberusException ex) {
            response.setContentType("text/html");
            Logger.getLogger(AddCampaignParameter.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            response.getWriter().append("Unable to create parameter").close();
        }

    }
}
