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
package org.cerberus.servlet.publi;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import org.cerberus.entity.Parameter;
import org.cerberus.entity.Robot;
import org.cerberus.entity.TestBatteryContent;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ICampaignService;
import org.cerberus.service.ILogEventService;
import org.cerberus.service.IParameterService;
import org.cerberus.service.IRobotService;
import org.cerberus.service.ITestBatteryService;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.impl.LogEventService;
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
    private ITestCaseExecutionService testCaseExecutionService;
    private IRobotService robotService;
    private IParameterService parameterService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createPublicCalls("/GetCampaignExecutionsCommand", "CALL", "GetCampaignExecutionsCommandV0 called : " + request.getRequestURL(), request);

        campaignService = appContext.getBean(ICampaignService.class);
        testBatteryService = appContext.getBean(ITestBatteryService.class);
        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        parameterService = appContext.getBean(IParameterService.class);

        String campaignId = policy.sanitize(request.getParameter("campaign"));
        String campaignName = policy.sanitize(request.getParameter("campaignname"));

        String robotName = policy.sanitize(request.getParameter("robot"));

        String fromStr = policy.sanitize(request.getParameter("from"));
        String onStr = policy.sanitize(request.getParameter("on"));

        String url;

        try {
            Parameter cerberusURL = parameterService.findParameterByKey("cerberus_url", null);
            url = cerberusURL.getValue();
        } catch (CerberusException ex) {
            url = request.getParameter("url");
            Logger.getLogger(GetCampaignExecutionsCommand.class.getName()).log(Level.SEVERE, null, ex);
        }

        url += "/RunTestCase?"
                //                + "redirect=Y"
                + "&__QUERY__"
                + "&ss_ip=__HOST__"
                + "&ss_p=__PORT__"
                + "&Tag=__TAG__"
                + "&screenshot=__SCREEN__";

        String host;
        String port;
        String screenshot;
        String tag;

        if (robotName != null && !"".equals(robotName.trim())) {
            try {
                robotService = appContext.getBean(IRobotService.class);
                Robot robot = robotService.findRobotByName(robotName);
                host = robot.getHost();
                port = robot.getPort();

            } catch (CerberusException ex) {
                host = "";
                port = "";
                Logger.getLogger(GetCampaignExecutionsCommand.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        } else {
            host = policy.sanitize(request.getParameter("host"));
            port = policy.sanitize(request.getParameter("port"));
        }

        screenshot = policy.sanitize(request.getParameter("screenshot"));
        if (screenshot == null || "".equals(screenshot.trim())) {
            screenshot = "0";
        }

        tag = policy.sanitize(request.getParameter("tag"));
        String notOnTag = policy.sanitize(request.getParameter("notOnTag"));


        url = url.replaceAll("__HOST__", host)
                .replaceAll("__PORT__", port)
                .replaceAll("__TAG__", tag)
                .replaceAll("__SCREEN__", screenshot);

        PrintWriter printWriter = response.getWriter();

        try {
            Campaign campaign;
            if (campaignName != null && !"".equals(campaignName.trim())) {
                campaign = campaignService.findCampaignByCampaignName(campaignName);
            } else {
                campaign = campaignService.findCampaignByKey(Integer.parseInt(campaignId));
            }

            List<CampaignContent> campaignContentList = campaignService.findCampaignContentsByCampaignName(campaign.getCampaign());

            List<String> testCaseExecutionAlreadyOnTag = new ArrayList<String>();
            if(notOnTag != null && !"".equals(notOnTag.trim())) {
                testCaseExecutionAlreadyOnTag = convertTestCaseExecutionsToListOfKeys(testCaseExecutionService.findExecutionsByCampaignNameAndTag(campaignName, notOnTag));
            }
            
            List<CampaignParameter> campaignParameterList = campaignService.findCampaignParametersByCampaignName(campaign.getCampaign());

            List<String> queries = convertListOfContentToListOfQueries(campaignContentList,testCaseExecutionAlreadyOnTag);
            List<String> listOfQueriesForCampaignParameters = convertParametersListToListOfQueries(campaignParameterList, queries);

            if (fromStr != null && !"".equals(fromStr.trim()) && onStr != null && !"".equals(onStr.trim())) {
                Integer from = Integer.valueOf(fromStr);
                Integer on = Integer.valueOf(onStr);

                if (from <= on && from > 0) {
                    listOfQueriesForCampaignParameters = splitQueriesFromTo(listOfQueriesForCampaignParameters, from, on);
                }
            }

            for (String query : listOfQueriesForCampaignParameters) {
                printWriter.append(url.replaceAll("__QUERY__", query)).append("\r\n");
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

    private List<String> convertTestCaseExecutionsToListOfKeys(List<TestCaseExecution> testCaseExecutions) {
        List<String> testCaseExecutionKeys = new ArrayList<String>();
        if(testCaseExecutions != null && testCaseExecutions.size() > 0) {
            String key;
            for (TestCaseExecution testCaseExecution : testCaseExecutions) {
                key = testCaseExecution.getTest()+"_"+testCaseExecution.getTestCase();
                testCaseExecutionKeys.add(key);
            }
        }
        return testCaseExecutionKeys;
        
    }

    private List<String> convertListOfContentToListOfQueries(List<CampaignContent> campaignContentList, List<String>  testCaseExecutionAlreadyOnTag) {
        List<String> queries = new ArrayList<String>();

        List<TestBatteryContent> testBatteryContents;

        StringBuilder sb;
        String key;
        for (CampaignContent campaignContent : campaignContentList) {
            try {
                testBatteryContents = testBatteryService.findTestBatteryContentsByTestBatteryName(campaignContent.getTestbattery());
                for (TestBatteryContent testBatteryContent : testBatteryContents) {
                    key = testBatteryContent.getTest() + "_" + testBatteryContent.getTestCase();
                    if( !testCaseExecutionAlreadyOnTag.contains(key)) {
                        sb = new StringBuilder("&Test=")
                                .append(URLEncoder.encode(testBatteryContent.getTest(), "UTF-8"))
                                .append("&TestCase=")
                                .append(URLEncoder.encode(testBatteryContent.getTestCase(), "UTF-8"));
                        if (!queries.contains(sb.toString())) {
                            queries.add(sb.toString());
                        }
                    }
                }
            } catch (CerberusException ex) {
                Logger.getLogger(GetCampaignExecutionsCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
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
                try {
                    sb.append("&")
                            .append(parameter.substring(0, 1).toUpperCase())
                            .append(parameter.substring(1).toLowerCase())
                            .append("=")
                            .append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(GetCampaignExecutionsCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                queriesTmp.add(sb.toString());
            }
        }

        return queriesTmp;
    }

    private List<String> splitQueriesFromTo(List<String> queries, int from, int on) {
        int interval = queries.size() / on;

        if (from < on) {
            on = from * interval;
        } else {
            on = queries.size();
        }

        from = (from - 1) * interval;

        return queries.subList(from, on);
    }
}
