/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.servlet.zzpublic;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.Campaign;
import org.cerberus.crud.entity.CampaignContent;
import org.cerberus.crud.entity.TestBatteryContent;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.service.ICampaignContentService;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.ITestBatteryContentService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.api.GetableHttpServlet;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.validity.Validity;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Get information about a campaign.
 * <p>
 * Campaign can be fetched by its id, as explained from {@link Request}
 * Information is
 *
 * @author Aurelien Bourdon
 */
public class GetCampaignInformation extends GetableHttpServlet<GetCampaignInformation.Request, GetCampaignInformation.Response> {

    /**
     * The servlet parameters.
     *
     * @author Aurelien Bourdon
     */
    public static class Request implements Validity {
        /**
         * The mandatory request's campaign identifier
         */
        public static final String CAMPAIGN_ID_PARAMETER = "id";
        private final String campaignId;

        /**
         * The request's fail if missing option
         */
        public static final String FAIL_IF_MISSING_PARAMETER = "failIfMissing";
        private boolean failIfMissing;

        public Request(final String campaignId) {
            this.campaignId = campaignId;
        }

        @Override
        public boolean isValid() {
            try {
                Integer.parseInt(campaignId);
                return true;
            } catch (final NumberFormatException e) {
                return false;
            }
        }

        public Integer getCampaignId() {
            return Integer.parseInt(campaignId);
        }

        public void setFailIfMissing(final boolean failIfMissing) {
            this.failIfMissing = failIfMissing;
        }

        public boolean isFailIfMissing() {
            return failIfMissing;
        }
    }

    /**
     * The servlet response.
     *
     * @author Aurelien Bourdon
     */
    public static class Response {
        private final List<TestCase> testCases;

        public Response(final List<TestCase> testCases) {
            this.testCases = testCases;
        }

        public List<TestCase> getTestCases() {
            return testCases;
        }
    }

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(GetCampaignInformation.class);

    /**
     * The associated {@link ApplicationContext}
     *
     * @see #init() for more initialization details
     */
    private ApplicationContext applicationContext;

    @Override
    public void init() throws ServletException {
        super.init();
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    }

    @Override
    protected Request parseRequest(final HttpServletRequest req) throws RequestParsingException {
        final Request request = new Request(req.getParameter(Request.CAMPAIGN_ID_PARAMETER));
        request.setFailIfMissing(Boolean.parseBoolean(req.getParameter(Request.FAIL_IF_MISSING_PARAMETER)));
        return request;
    }

    @Override
    protected Response processRequest(final Request request) throws RequestProcessException {
        final List<TestCase> testCases = new ArrayList<>();
        for (final CampaignContent campaignContent : retrieveCampaignContents(retrieveCampaign(request.getCampaignId()))) {
            for (final TestBatteryContent testBatteryContent : retrieveTestBatteryContents(campaignContent)) {
                try {
                    testCases.add(retrieveTestCase(testBatteryContent));
                } catch (final RequestProcessException exception) {
                    if (request.isFailIfMissing()) {
                        throw exception;
                    }
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Fail if missing disabled but exception thrown when getting test case", exception);
                    }
                }
            }
        }
        return new Response(testCases);
    }

    @Override
    protected String getUsageDescription() {
        return "Get information about a campaign.\n" +
                "\n" +
                "Usage: GET /<host>?id=<campaignIdentifier>[failIfMissing=true]\n" +
                "\n" +
                "Request parameters\n" +
                "\n" +
                "- id, mandatory: the campaign identifier\n" +
                "- failIfMissing, optional: if campaign is containing test cases which cannot be found\n" +
                "\n" +
                "Expected response (in case of success)\n" +
                "\n" +
                "{\n" +
                "  testCases: [List of campaign's test cases]\n" +
                "}\n";
    }

    private Campaign retrieveCampaign(final Integer campaignId) throws RequestProcessException {
        final ICampaignService campaignService = applicationContext.getBean(ICampaignService.class);
        Campaign campaign;
        try {
            campaign = campaignService.findCampaignByKey(campaignId);
        } catch (final CerberusException exception) {
            throw new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get campaign", exception);
        }
        if (campaign == null) {
            throw new RequestProcessException(HttpStatus.NOT_FOUND, "No campaign found");
        }
        return campaign;
    }

    private List<CampaignContent> retrieveCampaignContents(final Campaign campaign) throws RequestProcessException {
        final ICampaignContentService campaignContentService = applicationContext.getBean(ICampaignContentService.class);
        final AnswerList<CampaignContent> campaignContents = campaignContentService.readByCampaign(campaign.getCampaign());
        if (!MessageEventEnum.DATA_OPERATION_OK.equals(campaignContents.getResultMessage().getSource())) {
            if (MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND.equals(campaignContents.getResultMessage().getSource())) {
                return Collections.emptyList();
            }
            throw new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to get campaign's test batteries: " + campaignContents.getMessageDescription());
        }
        return campaignContents.getDataList();
    }

    private List<TestBatteryContent> retrieveTestBatteryContents(final CampaignContent campaignContent) throws RequestProcessException {
        final ITestBatteryContentService testBatteryContentService = applicationContext.getBean(ITestBatteryContentService.class);
        final AnswerList<TestBatteryContent> testBatteryContents = testBatteryContentService.readByTestBattery(campaignContent.getTestbattery());
        if (!MessageEventEnum.DATA_OPERATION_OK.equals(testBatteryContents.getResultMessage().getSource())) {
            if (MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND.equals(testBatteryContents.getResultMessage().getSource())) {
                return Collections.emptyList();
            }
            throw new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to content for test battery " + campaignContent.getTestbattery());
        }
        return testBatteryContents.getDataList();
    }

    private TestCase retrieveTestCase(final TestBatteryContent testBatteryContent) throws RequestProcessException {
        final ITestCaseService testCaseService = applicationContext.getBean(ITestCaseService.class);
        final AnswerItem<TestCase> testCase = testCaseService.readByKey(testBatteryContent.getTest(), testBatteryContent.getTestCase());
        if (!MessageEventEnum.DATA_OPERATION_OK.equals(testCase.getResultMessage().getSource())) {
            throw MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND.equals(testCase.getResultMessage().getSource()) ?
                    new RequestProcessException(HttpStatus.NOT_FOUND, "Unable to find test case with test " + testBatteryContent.getTest() + " and identier " + testBatteryContent.getTestCase()) :
                    new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find test case due to " + testCase.getResultMessage());
        }
        return testCase.getItem();
    }

}
