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
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.servlet.api.info.GetableHttpServletInfo;
import org.cerberus.servlet.api.info.RequestParameter;
import org.cerberus.servlet.api.mapper.DefaultJsonHttpMapper;
import org.cerberus.servlet.api.mapper.HttpMapper;
import org.cerberus.servlet.api.zzpublic.PublicGetableHttpServlet;
import org.cerberus.util.StringUtil;
import org.cerberus.util.validity.Validable;
import org.cerberus.util.validity.Validity;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Get a list of test cases based on criteria
 *
 * @author Aurelien Bourdon
 */
public class GetTestCases extends PublicGetableHttpServlet<GetTestCases.Request, GetTestCases.Response> {

    /**
     * The associated request to this {@link GetTestCases} servlet
     *
     * @author Aurelien Bourdon
     */
    public static class Request implements Validable {

        public static final RequestParameter APPLICATION = new RequestParameter("application", "Application name criteria. Allow to search the list of test cases associated to an application.");
        private final String application;

        public Request(final String application) {
            this.application = application;
        }

        public String getApplication() {
            return application;
        }

        @Override
        public Validity validate() {
            final Validity.Builder validity = Validity.builder();
            if (StringUtil.isNullOrEmpty(application)) {
                validity.reason(String.format("null or empty `%s` parameter", APPLICATION.getName()));
            }
            return validity.build();
        }

    }

    /**
     * The associated response to this {@link GetTestCases} servlet
     *
     * @author Aurelien Bourdon
     */
    public static class Response {

        public static class TestCase {
            private final String test;
            private final String testCase;
            private final String description;
            private final String application;
            private final String status;

            public TestCase(final String test, final String testCase, final String description, final String application, final String status) {
                this.test = test;
                this.testCase = testCase;
                this.description = description;
                this.application = application;
                this.status = status;
            }

            public String getTest() {
                return test;
            }

            public String getTestCase() {
                return testCase;
            }

            public String getDescription() {
                return description;
            }

            public String getApplication() {
                return application;
            }

            public String getStatus() {
                return status;
            }
        }

        private final List<TestCase> testCases;

        public Response() {
            testCases = new ArrayList<>();
        }

        public void addTestCase(final TestCase testCase) {
            testCases.add(testCase);
        }

        public List<TestCase> getTestCases() {
            return testCases;
        }
    }


    private static final String VERSION = "V1";
    private ITestCaseService testCaseService;

    @Override
    protected void postInit() throws ServletException {
        testCaseService = getApplicationContext().getBean(ITestCaseService.class);
    }

    @Override
    protected String getVersion() {
        return VERSION;
    }

    @Override
    protected GetableHttpServletInfo getInfo() {
        return new GetableHttpServletInfo(
                GetTestCases.class.getSimpleName(),
                getVersion(),
                "Get a list of test cases based on criteria",
                new GetableHttpServletInfo.GetableUsage(
                        Sets.newHashSet(
                                Request.APPLICATION
                        )
                )
        );
    }

    @Override
    protected HttpMapper getHttpMapper() {
        return new DefaultJsonHttpMapper();
    }

    @Override
    protected Request parseRequest(final HttpServletRequest req) throws RequestParsingException {
        return new Request(req.getParameter(Request.APPLICATION.getName()));
    }

    @Override
    protected Response processRequest(final Request request) throws RequestProcessException {
        return convertTestCasesToResponse(findTestCasesByApplication(request.getApplication()));
    }

    private List<TestCase> findTestCasesByApplication(final String application) throws RequestProcessException {
        final List<TestCase> testCases = testCaseService.findTestCaseByApplication(application);
        if (testCases == null) {
            throw new RequestProcessException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Unable to find test cases for application '%s'. Contact your administrator", application));
        }
        return testCases;
    }

    private Response convertTestCasesToResponse(final List<TestCase> testCases) {
        final Response response = new Response();
        for (final TestCase testCase : testCases) {
            response.addTestCase(new Response.TestCase(
                    testCase.getTest(),
                    testCase.getTestCase(),
                    testCase.getDescription(),
                    testCase.getApplication(),
                    testCase.getStatus()
            ));
        }
        return response;
    }

}
