/* Cerberus Copyright (C) 2013 - 2017 cerberustesting
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

 This file is part of Cerberus.

 Cerberus is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Cerberus is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.*/
package org.cerberus.servlet.zzpublic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.util.StringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This webservices is the rewriting of GetTestCasesV000 servlet adding Jax-RS
 * Jax-RS is Java API for RESTful Web Services To learn more about it :
 * http://spoonless.github.io/epsi-b3-javaee/javaee_web/jaxrs.html
 *
 * Webservice target it to return testcase by application
 *
 * @author Corentin Delage
 */
@Path("/GetTestCasesV001")
public class GetTestCasesV001 {

    private ITestCaseService testCaseService;
    private static final String VERSION = "V2";
    private static final Logger LOG = LogManager.getLogger(GetTestCasesV001.class);

    /*
    * Response.Testcase object target is to construct testcase object formatted for webservice response
    * expected attributes is : test, testCase, description, application, status.
     */
    public static class ResponseTC {

        public static class TestCase implements Serializable {

            // TO DO : rework with auto generated serialization, this method should create bug when other object are import with same serialVersionUID
            private static final long serialVersionUID = 1L;

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
    }

    protected String getVersion() {
        return VERSION;
    }

    /*
     *method getTestCaseByApplication 
     *Called by user GET request
     *target it build response send to user
     *@Param application : get value of application in query url (to learn more about @QueryParam : https://www.mkyong.com/webservices/jax-rs/jax-rs-queryparam-example/)
     *@Param servletContext : get servlet context to get bean of application in fine.
     *@Return response : return the list of application in success case or error message in failed case
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTestCaseByApplication(@QueryParam("application") String application, @Context ServletContext servletContext) {
        LOG.info("Webservice GetTestCasesV001 called by GET Request");
        Map<String, Object> mapResponse = new HashMap<>();
        try {
            if (!StringUtil.isNullOrEmpty(application)) {
                //Process to get testcase by application
                List<ResponseTC.TestCase> testCaseListResponse = new ArrayList<>();
                testCaseListResponse = findTestCasesByApplication(application, servletContext);
                mapResponse.put("testCases", testCaseListResponse);
            } else {
                //TO DO : rework for get same exception than GetTestCasesV000
                mapResponse.put("Version", getVersion());
                mapResponse.put("Error", "no parameter application found");
            }
        } catch (Exception exception) {
            //TO DO : rework for get same exception than GetTestCasesV000
            mapResponse.put("Version", getVersion());
            mapResponse.put("Error", exception.getMessage());
        }
        //Final response send to client
        //Note : is also possible to return mapResponse only, Jax-RS will convert it to JSON (Response is Jax-RS Object)
        return Response.ok(mapResponse).build();
    }

    /*
     * Method findTestCasesByApplication
     * target is return list of testcase associated to an application
     * @Param application
     * @Param ServletContext : using to call getTestCaseServiceFromSpring, to get testcase service spring bean
     * @Return testCases : list of response
     */
    private List<ResponseTC.TestCase> findTestCasesByApplication(final String application, ServletContext servletContext) {
        //Process to get testCase by application
        getTestCaseServiceFromSpring(servletContext);
        List<ResponseTC.TestCase> response = new ArrayList<>();
        final List<TestCase> testCases = testCaseService.findTestCaseByApplication(application);

        if (testCases == null) {
            LOG.error("TestCase list for application {} is null", application);
        } else {
            //Called conversion from testcase to Response.testcase
            response = testCaseListConversionToResponse(testCases);
        }
        return response;
    }

    /*
     * Method getTestCaseServiceFromSpring
     * target is to get bean of testcase service instance from the current application.
     * @Param servletContext : context of servlet catched when GetTesCasesV001 is called 
     */
    public void getTestCaseServiceFromSpring(ServletContext servletContext) {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        this.testCaseService = appContext.getBean(ITestCaseService.class);
    }

    /*
     * Method testCaseListConvertToResponse
     * target is convert testcase from list to ResponseTC.testcase (to keep expected element of testcase for webservice response)
     * expected element : test, testCase, description, application, status
     * @Param testCaseList
     * @Return testCaseList : reworked testcaseList
     */
    public List<ResponseTC.TestCase> testCaseListConversionToResponse(List<TestCase> testCaseList) {
        //using stream method, to learn more about it : https://blog.ippon.fr/2014/03/17/api-stream-une-nouvelle-facon-de-gerer-les-collections-en-java-8/
        List<ResponseTC.TestCase> responseList = testCaseList.stream()
                .map(testCase -> new ResponseTC.TestCase(testCase.getTest(),
                testCase.getTestCase(), testCase.getDescription(),
                testCase.getApplication(), testCase.getStatus()))
                .collect(Collectors.toList());
        return responseList;
    }
}
