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

import org.cerberus.crud.entity.*;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseDep;
import org.cerberus.crud.factory.IFactoryTestCaseLabel;
import org.cerberus.crud.service.*;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractCreateUpdateTestCase extends AbstractCrudTestCase {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(AbstractCreateUpdateTestCase.class);
    @Autowired
    private IFactoryTestCaseLabel testCaseLabelFactory;
    @Autowired
    private IFactoryTestCaseCountry testCaseCountryFactory;
    @Autowired
    private IFactoryTestCaseDep testCaseDepFactory;
    @Autowired
    private ITestCaseLabelService testCaseLabelService;
    @Autowired
    private ITestCaseCountryService testCaseCountryService;
    @Autowired
    private ITestCaseCountryPropertiesService testCaseCountryPropertiesService;
    @Autowired
    private ITestCaseStepService testCaseStepService;
    @Autowired
    private ITestCaseStepActionService testCaseStepActionService;
    @Autowired
    private ITestCaseStepActionControlService testCaseStepActionControlService;
    @Autowired
    private ITestCaseDepService testCaseDepService;

    protected abstract String getTypeOperation();

    protected abstract void fireLogEvent(String keyTest, String keyTestCase, TestCase tc, HttpServletRequest request, HttpServletResponse response);

    protected abstract TestCase getTestCaseBeforeTraitment(String keyTest, String keyTestCase) throws CerberusException, UnsupportedEncodingException;

    protected abstract void updateTestCase(String originalTest, String originalTestCase, TestCase tc) throws CerberusException;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, CerberusException, JSONException {

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
        String testcase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("testCase"), "");
        String originalTest = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTest"), "");
        String originalTestCase = ParameterParserUtil.parseStringParamAndSanitize(request.getParameter("originalTestCase"), "");

        boolean primaryKeyChanged = !(originalTest != null && originalTestCase != null && originalTest.equals(test) && originalTestCase.equals(testcase));

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(test) && StringUtil.isNullOrEmpty(testcase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", this.getTypeOperation())
                    .replace("%REASON%", "mandatory fields (test or testcase) are missing."));
            finalAnswer.setResultMessage(msg);

        } else {
            try {
                TestCase tc = getTestCaseFromRequest(request, getTestCaseBeforeTraitment(originalTest, originalTestCase));
                updateTestCase(originalTest, originalTestCase, tc);

                if (StringUtil.isNullOrEmpty(originalTest)) {
                    fireLogEvent(test, testcase, tc, request, response);
                } else {
                    fireLogEvent(originalTest, originalTestCase, tc, request, response);
                }

                // Update labels
                if (request.getParameter("labels") != null) {
                    JSONArray objLabelArray = new JSONArray(request.getParameter("labels"));
                    List<TestCaseLabel> labelList = getLabelListFromRequest(request, test, testcase, objLabelArray);

                    // Update the Database with the new list.
                    ans = testCaseLabelService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestCase(), labelList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                }

                // Update Countries
                if (request.getParameter("countries") != null) {
                    JSONArray objCountryArray = new JSONArray(request.getParameter("countries"));
                    List<TestCaseCountry> tccList = getCountryListFromRequest(request, test, testcase, objCountryArray);

                    // Update the Database with the new list.
                    ans = testCaseCountryService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestCase(), tccList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                }

                // Update Countries
                if (request.getParameter("countries") != null) {
                    JSONArray objCountryArray = new JSONArray(request.getParameter("countries"));
                    List<TestCaseCountry> tccList = getCountryListFromRequest(request, test, testcase, objCountryArray);

                    // Update the Database with the new list.
                    ans = testCaseCountryService.compareListAndUpdateInsertDeleteElements(test, testcase, tccList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

                    // Duplicate other objects.
                    List<TestCaseCountryProperties> tccpList;
                    List<TestCaseCountryProperties> newTccpList = new ArrayList<>();
                    if (primaryKeyChanged && !tccList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        tccpList = testCaseCountryPropertiesService.findListOfPropertyPerTestTestCase(originalTest, originalTestCase);
                        //Build a new list with the countries that exist for the testcase.
                        for (TestCaseCountryProperties curTccp : tccpList) {
                            if (testCaseCountryService.exist(test, testcase, curTccp.getCountry())) {
                                newTccpList.add(curTccp);
                            }
                        }
                        if (!newTccpList.isEmpty()) {
                            ans = testCaseCountryPropertiesService.duplicateList(newTccpList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }

                }

                // update testcase dependency
                if (request.getParameter("dependencies") != null) {
                    List<TestCaseDep> tcdList = getDependencyFromRequest(request, tc);

                    testCaseDepService.compareListAndUpdateInsertDeleteElements(tc.getTest(), tc.getTestCase(), tcdList);
                }

                if (primaryKeyChanged) {
                    List<TestCaseStep> tcsList = new ArrayList<>();
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        tcsList = testCaseStepService.getListOfSteps(originalTest, originalTestCase);
                        if (!tcsList.isEmpty()) {
                            ans = testCaseStepService.duplicateList(tcsList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }

                    List<TestCaseStepAction> tcsaList = new ArrayList<>();
                    if (!tcsList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        tcsaList = testCaseStepActionService.findTestCaseStepActionbyTestTestCase(originalTest, originalTestCase);
                        if (!tcsaList.isEmpty()) {
                            ans = testCaseStepActionService.duplicateList(tcsaList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }

                    if (!tcsList.isEmpty() && !tcsaList.isEmpty() && ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        List<TestCaseStepActionControl> tcsacList = testCaseStepActionControlService.findControlByTestTestCase(originalTest, originalTestCase);
                        if (!tcsacList.isEmpty()) {
                            ans = testCaseStepActionControlService.duplicateList(tcsacList, test, testcase);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                        }
                    }
                }
            } catch (CerberusException ex) {
                LOG.error(" Exception :" + ex.toString(), ex);

                MessageEvent msgEx = new MessageEvent(MessageEventEnum.GENERIC_ERROR);
                msgEx.setDescription(msg.getDescription() + " " + ex.getMessageError().getDescription());
                finalAnswer.setResultMessage(msgEx);
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    private List<TestCaseCountry> getCountryListFromRequest(HttpServletRequest request, String test, String testCase, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<TestCaseCountry> tdldList = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            String country = objectJson.getString("country");
            // Parameter that needs to be secured --> We SECURE+DECODE them
            // NONE
            // Parameter that we cannot secure as we need the html --> We DECODE them

            if (!delete) {
                TestCaseCountry tcc = testCaseCountryFactory.create(test, testCase, country);
                tdldList.add(tcc);
            }
        }
        return tdldList;
    }

    protected List<TestCaseLabel> getLabelListFromRequest(HttpServletRequest request, String test, String testCase, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<TestCaseLabel> labelList = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            Integer labelId = objectJson.getInt("labelId");

            Timestamp creationDate = new Timestamp(new Date().getTime());

            if (!delete) {
                labelList.add(testCaseLabelFactory.create(0, test, testCase, labelId, request.getRemoteUser(), creationDate, request.getRemoteUser(), creationDate, null));
            }
        }
        return labelList;
    }

    protected List<TestCaseDep> getDependencyFromRequest(HttpServletRequest request, TestCase tc) throws JSONException {
        List<TestCaseDep> res = new LinkedList<>();
        jsonArrayFoEach(request, "dependencies", (jsonObj) -> {
            String testcase = jsonObj.getString("testcase");
            Long testcaseid = jsonObj.getLong("id");
            String test = jsonObj.getString("test");
            String description = jsonObj.getString("description");

            String active = jsonObj.getString("active");
            if (Boolean.valueOf(active)) {
                active = "Y";
            } else {
                active = "N";
            }

            Timestamp creationDate = new Timestamp(new Date().getTime());

            res.add(testCaseDepFactory.create(testcaseid, tc.getTest(), tc.getTestCase(), test, testcase, "", TestCaseExecutionQueueDep.TYPE_TCEXEEND, active, description, request.getRemoteUser(), creationDate, request.getRemoteUser(), creationDate));
        }
        );

        return res;
    }

    @FunctionalInterface
    protected interface JsonFunction<T> {

        void foreach(T t) throws JSONException;
    }

    protected void jsonArrayFoEach(HttpServletRequest request, String jsonArrayName, JsonFunction<JSONObject> fct) throws JSONException {
        JSONArray json = new JSONArray(request.getParameter(jsonArrayName));

        for (int i = 0; i < json.length(); i++) {
            fct.foreach(json.getJSONObject(i));
        }

    }
}
