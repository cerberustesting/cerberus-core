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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.entity.Campaign;
import org.cerberus.entity.CampaignContent;
import org.cerberus.entity.CampaignParameter;
import org.cerberus.entity.Robot;
import org.cerberus.entity.TestBatteryContent;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICampaignService;
import org.cerberus.service.IRobotService;
import org.cerberus.service.ITestBatteryService;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author memiks
 */
@WebServlet(name = "GetCampaignExecutionsCommand", urlPatterns = {"/GetCampaignExecutionsCommand"})
public class GetCampaignExecutionsCommand extends HttpServlet {

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
    private ICampaignService campaignService;
    private ITestBatteryService testBatteryService;
    private IRobotService robotService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        campaignService = appContext.getBean(ICampaignService.class);
        testBatteryService = appContext.getBean(ITestBatteryService.class);

        String campaignId = policy.sanitize(request.getParameter("campaign"));

        String robotName = policy.sanitize(request.getParameter("robot"));

        String host;
        String port;

        if (robotName != null && !"".equals(robotName.trim())) {
            try {
                robotService = appContext.getBean(IRobotService.class);
                Robot robot = robotService.findRobotByName(robotName);
                host = robot.getHost();
                port = robot.getPort();

            } catch (CerberusException ex) {
                Logger.getLogger(GetCampaignExecutionsCommand.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        } else {
            host = policy.sanitize(request.getParameter("host"));
            port = policy.sanitize(request.getParameter("port"));
        }

        PrintWriter printWriter = response.getWriter();

        try {
            Campaign campaign = campaignService.findCampaignByKey(Integer.parseInt(campaignId));

            List<CampaignContent> campaignContentList = campaignService.findCampaignContentsByCampaignName(campaign.getCampaign());
            List<CampaignParameter> campaignParameterList = campaignService.findCampaignParametersByCampaignName(campaign.getCampaign());

            List<String> queries = convertListOfContentToListOfQueries(campaignContentList);
            List<String> listOfQueriesForCampaignParameters = convertParametersListToListOfQueries(campaignParameterList, queries);

            for (String query : listOfQueriesForCampaignParameters) {
                printWriter.append(query).append("<br>\n\r");
            }

            printWriter.close();

        } catch (CerberusException ex) {
            response.getWriter().print(ex.getMessageError().getDescription());
        }
    }

    private List<String> convertParametersListToListOfQueries(List<CampaignParameter> campaignParameterList, List<String> queries) {
        HashMap<String, List<String>> hmParametersValues = new HashMap<String, List<String>>();

        for (CampaignParameter campaignParameter : campaignParameterList) {
            if (campaignParameter.getParameter() != null && campaignParameter.getValue() != null) {
                List<String> values;

                if (hmParametersValues.containsKey(campaignParameter.getParameter())) {
                    values = hmParametersValues.get(campaignParameter.getParameter());
                } else {
                    values = new ArrayList<String>();
                }

                if (!values.contains(campaignParameter.getValue())) {
                    values.add(campaignParameter.getValue());
                }

                hmParametersValues.put(campaignParameter.getParameter(), values);
            }
        }

        for (Map.Entry<String, List<String>> entry : hmParametersValues.entrySet()) {
            String parameter = entry.getKey();
            List<String> values = entry.getValue();

            queries = convertHashMapParametersToListOfQueryString(parameter, values, queries);
        }

        return queries;
    }

    private List<String> convertListOfContentToListOfQueries(List<CampaignContent> campaignContentList) {
        List<String> queries = new ArrayList<String>();

        List<TestBatteryContent> testBatteryContents;

        StringBuilder sb;
        for (CampaignContent campaignContent : campaignContentList) {
            try {
                testBatteryContents = testBatteryService.findTestBatteryContentsByTestBatteryName(campaignContent.getTestbattery());
                for (TestBatteryContent testBatteryContent : testBatteryContents) {
                    sb = new StringBuilder("&Test=")
                            .append(testBatteryContent.getTest())
                            .append("&TestCase=")
                            .append(testBatteryContent.getTestCase());
                    if (!queries.contains(sb.toString())) {
                        queries.add(sb.toString());
                    }
                }
            } catch (CerberusException ex) {
                Logger.getLogger(GetCampaignExecutionsCommand.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return queries;
    }

    private List<String> convertHashMapParametersToListOfQueryString(String parameter, List<String> values, List<String> queries) {
        List<String> queriesTmp = new ArrayList<String>();
        StringBuilder sb;
        for (String query : queries) {
            for (String value : values) {
                sb = new StringBuilder(query);
                sb.append("&").append(parameter).append("=").append(value);
                queriesTmp.add(sb.toString());
            }
        }

        return queriesTmp;
    }
}
