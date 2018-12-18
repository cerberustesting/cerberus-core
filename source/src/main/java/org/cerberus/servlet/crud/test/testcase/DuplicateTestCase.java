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
package org.cerberus.servlet.crud.test.testcase;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseLabel;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseLabelService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.impl.InvariantService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet implementation class DuplicateTest
 * @WebServlet(name = "DuplicateTestCase", urlPatterns = {"/DuplicateTestCase"})
 * @Deprecated use CreateTestCase instead of this class
 */
@WebServlet(name = "DuplicateTestCase", urlPatterns = {"/DuplicateTestCase"})
@Deprecated  // Can we delete it ??
public class DuplicateTestCase extends AbstractCrudTestCase {

    private static final Logger LOG = LogManager.getLogger(DuplicateTestCase.class);
    private static final long serialVersionUID = 1L;

    @Autowired
    private ITestCaseService testCaseService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService ;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private ITestCaseLabelService testCaseLabelService;
    @Autowired
    private ILogEventService logEventService;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException, CerberusException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        String test = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("test"), "");
        String testCase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("testCase"), "");
        String originalTest = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTest"), "");
        String originalTestCase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTestCase"), null);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(test) || StringUtil.isNullOrEmpty(testCase)
                || StringUtil.isNullOrEmpty(originalTest) || originalTestCase != null) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Duplicate")
                    .replace("%REASON%", "mandatory fields are missing."));
            ans.setResultMessage(msg);
        } else {
            AnswerItem originalTestAI = testCaseService.readByKey(originalTest, originalTestCase);
            AnswerItem targetTestAI = testCaseService.readByKey(test, testCase);

            TestCase originalTC = (TestCase) originalTestAI.getItem();
            TestCase targetTC = (TestCase) targetTestAI.getItem();
            if (!(originalTestAI.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && originalTestAI.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Duplicate")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else /**
             * The service was able to perform the query and confirm the object
             * exist, then we can update it.
             */
            if (!request.isUserInRole("Test")) { // We cannot update the testcase if the user is not at least in Test role.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Duplicate")
                        .replace("%REASON%", "Not enought privilege to duplicate the testcase. You must belong to Test Privilege."));
                ans.setResultMessage(msg);

            } else if (targetTC != null) { // If target Test Case already exists.
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Duplicate")
                        .replace("%REASON%", "The test case you try to create already exists. Please define a test/testcase that is not already existing."));
                ans.setResultMessage(msg);

            } else {
                this.getTestCaseFromRequest(request, originalTC);

                //Update object with new testcase id and insert it in db
                originalTC.setTest(test);
                originalTC.setTestCase(testCase);
                ans = testCaseService.create(originalTC);

                List<TestCaseCountry> countryList = new ArrayList<>();
                countryList = testCaseCountryService.findTestCaseCountryByTestTestCase(originalTest, originalTestCase);
                boolean success = true;
                if (!countryList.isEmpty()) {
                        ans = testCaseCountryService.duplicateList(countryList, test, testCase);
                    }

                List<TestCaseCountryProperties> tccpList = new ArrayList<>();
                if (!countryList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && success) {
                    tccpList = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCase(originalTest, originalTestCase);
                    if (!tccpList.isEmpty()) {
                        ans = testCaseCountryPropertiesService.duplicateList(tccpList, test, testCase);
                    }
                }

                List<TestCaseStep> tcsList = new ArrayList<>();
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && success) {
                    tcsList = testCaseStepService.getListOfSteps(originalTest, originalTestCase);
                    if (!tcsList.isEmpty()) {
                        ans = testCaseStepService.duplicateList(tcsList, test, testCase);
                    }
                }

                List<TestCaseStepAction> tcsaList = new ArrayList<>();
                if (!tcsList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && success) {
                    tcsaList = testCaseStepActionService.findTestCaseStepActionbyTestTestCase(originalTest, originalTestCase);
                    if (!tcsaList.isEmpty()) {
                        ans = testCaseStepActionService.duplicateList(tcsaList, test, testCase);
                    }
                }

                if (!tcsList.isEmpty() && !tcsaList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && success) {
                    List<TestCaseStepActionControl> tcsacList = testCaseStepActionControlService.findControlByTestTestCase(originalTest, originalTestCase);
                    if (!tcsacList.isEmpty()) {
                        ans = testCaseStepActionControlService.duplicateList(tcsacList, test, testCase);
                    }
                }

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && success) {
                    List<TestCaseLabel> tclList = testCaseLabelService.readByTestTestCase(originalTest, originalTestCase, null).getDataList();
                    if (!tclList.isEmpty()) {
                        ans = testCaseLabelService.duplicateList(tclList, test, testCase);
                    }
                }
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())
                        && success) {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Duplicate"));
                    ans.setResultMessage(msg);
                    /**
                     * Update was successful. Adding Log entry.
                     */
                    logEventService.createForPrivateCalls("/DuplicateTestCase", "CREATE", "Create testcase : ['" + test + "'|'" + testCase + "']", request);
                }

            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
    }

}
