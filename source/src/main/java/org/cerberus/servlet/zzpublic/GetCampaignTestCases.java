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

import com.google.common.collect.Sets;
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
import org.cerberus.servlet.api.mapper.HttpMapper;
import org.cerberus.servlet.api.info.GetableHttpServletInfo;
import org.cerberus.servlet.api.info.RequestParameter;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.validity.Validable;
import org.cerberus.util.validity.Validity;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Get {@link Campaign}'s {@link TestCase}s
 * <p>
 * Campaign can be fetched by its id, as defined in {@link Request}
 * <p>
 * Result is a list of {@link TestCase}s wrapped into a {@link Response}
 *
 * @author Aurelien Bourdon
 */
public class GetCampaignTestCases extends GetableHttpServlet<GetCampaignTestCases.Request, GetCampaignTestCases.Response> {

    /**
     * The servlet parameters.
     *
     * @author Aurelien Bourdon
     */
    public static class Request implements Validable {
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
        public Validity validate() {
            final Validity.Builder validity = Validity.builder();
            try {
                Integer.parseInt(campaignId);
            } catch (final NumberFormatException e) {
                validity.reason("bad integer format for `id`");
            }
            return validity.build();
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
    private static final Logger LOGGER = Logger.getLogger(GetCampaignTestCases.class);

    private HttpMapper httpMapper;

    @Override
    public void postInit() throws ServletException {
        httpMapper = new DefaultJsonHttpMapper();
    }

    @Override
    public HttpMapper getHttpMapper() {
        return httpMapper;
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
    protected GetableHttpServletInfo getInfo() {
        return new GetableHttpServletInfo(
                GetCampaignTestCases.class.getSimpleName(),
                getVersion(),
                "Get the list of test cases associated to a campaign",
                new GetableHttpServletInfo.GetableUsage(
                        Sets.newHashSet(
                                new RequestParameter("id", "the campaign identifier"),
                                new RequestParameter("failIfMissing", "if campaign is containing test cases which cannot be found", false)
                        )
                )
        );
    }

    private Campaign retrieveCampaign(final Integer campaignId) throws RequestProcessException {
        final ICampaignService campaignService = getApplicationContext().getBean(ICampaignService.class);
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
        final ICampaignContentService campaignContentService = getApplicationContext().getBean(ICampaignContentService.class);
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
        final ITestBatteryContentService testBatteryContentService = getApplicationContext().getBean(ITestBatteryContentService.class);
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
        final ITestCaseService testCaseService = getApplicationContext().getBean(ITestCaseService.class);
        final AnswerItem<TestCase> testCase = testCaseService.readByKey(testBatteryContent.getTest(), testBatteryContent.getTestCase());
        if (!MessageEventEnum.DATA_OPERATION_OK.equals(testCase.getResultMessage().getSource())) {
            throw MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND.equals(testCase.getResultMessage().getSource()) ?
                    new RequestProcessException(HttpStatus.NOT_FOUND, "Unable to find test case with test " + testBatteryContent.getTest() + " and identier " + testBatteryContent.getTestCase()) :
                    new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find test case due to " + testCase.getResultMessage());
        }
        return testCase.getItem();
    }

}
