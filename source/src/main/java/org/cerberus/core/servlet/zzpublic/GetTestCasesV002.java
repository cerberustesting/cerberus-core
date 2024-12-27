/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.servlet.zzpublic;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.ParameterParserUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Label;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.service.ILabelService;

/**
 *
 * @author Nouxx
 * @author MorganLmd
 *
 */
@WebServlet(name = "GetTestCasesV002", urlPatterns = {"/GetTestCasesV002"})
public class GetTestCasesV002 extends HttpServlet {

    private ITestCaseService testCaseService;
    private IAPIKeyService apiKeyService;
    private ILabelService labelService;

    private static final Logger LOG = LogManager.getLogger(GetTestCasesV002.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        // currently, the servlet only supports filtering on a single applicationParam
        String applicationParam = ParameterParserUtil.parseStringParam(request.getParameter("Application"), "");
        // currently, the servlet only supports filtering on a single typeParam
        String typeParam = ParameterParserUtil.parseStringParam(request.getParameter("Type"), "");
        // currently, the servlet only supports filtering on a single priorityParam
        String priorityParam = ParameterParserUtil.parseStringParam(request.getParameter("Priority"), "");
        // the servlet supports filtering on several labels id (AND operator)
        String labelsIdParam = ParameterParserUtil.parseStringParam(request.getParameter("Labels"), "");
        // the servlet supports filtering on several status (OR operator)
        String statusesParam = ParameterParserUtil.parseStringParam(request.getParameter("Statuses"), "");
        String testsParam = ParameterParserUtil.parseStringParam(request.getParameter("Tests"), "");

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createForPublicCalls("/GetTestCasesV002", "CALL", LogEvent.STATUS_INFO,
                "GetTestCasesV002 called : " + request.getRequestURL(), request);

        apiKeyService = appContext.getBean(IAPIKeyService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        labelService = appContext.getBean(ILabelService.class);

        if (apiKeyService.authenticate(request, response)) {
            List<TestCase> testcaseList;
            List<JSONObject> listOfTestCasesJSON = new ArrayList<>();
            try {
                // build JSON response object
                JSONObject jsonResponse = new JSONObject();

                // ignored parameters
                String[] searchTermTest = parseSingleValueFilter(testsParam);
                String[] searchTermCreator = null;
                String[] searchTermImplementer = null;
                String[] searchTermSystem = null;
                String[] searchTermCampaign = null;

                // currently only handling a single applicationParam
                String[] searchTermApplication = parseSingleValueFilter(applicationParam);
                // currently handling a single priorityParam
                String[] searchTermPriority = parseSingleValueFilter(priorityParam);
                // currently handling a single typeParam
                String[] searchTermType = parseSingleValueFilter(typeParam);
                List<Integer> searchTermLabelids = stringstoIntegerList(parseMultipleValuesFilter(labelsIdParam));
                String[] searchTermStatus = parseMultipleValuesFilter(statusesParam);

                // Get all testcases filtered by parameters
                testcaseList = testCaseService.readByVarious(searchTermTest, searchTermApplication,
                        searchTermCreator, searchTermImplementer, searchTermSystem, searchTermCampaign,
                        searchTermLabelids, searchTermPriority, searchTermType, searchTermStatus, -1).getDataList();

                // Create a Map of List of Testcase with the test as key
                Map<String, List<TestCase>> testcasesMapByTest = testcaseList
                        .stream()
                        .flatMap(testcase -> Stream.of(new SimpleEntry<>(testcase.getTest(), testcase)))
                        .collect(
                                Collectors.groupingBy(
                                        Map.Entry::getKey,
                                        Collectors.mapping(
                                                Map.Entry::getValue,
                                                Collectors.toList()
                                        )
                                )
                        );

                // We use the previous hashmap to get a list of Label
                // Then we iterate over each Testcase to add all its labels
                testcasesMapByTest
                        .forEach((test, testcases) -> {
                            HashMap<String, List<Label>> labelsMap = labelService.findLabelsFromTestCase(test, null, testcases);
                            testcases.forEach(testcase -> testcase.setLabels(labelsMap.get(testcase.getTest() + "##" + testcase.getTestcase())));
                        });

                // We filter the list of testcases to only keep those that have all the labels passed by parameters
                // only if the list of labels from query string is not null
                // Then we convert to json
                if(searchTermLabelids == null){
                    testcaseList
                        .stream()
                        .forEach(testcase -> listOfTestCasesJSON.add(testcase.toJson()));
                } else {
                    testcaseList
                        .stream()
                        .filter(testcase -> isTestcaseContainingAllLabelsParam(testcase, searchTermLabelids))
                        .forEach(testcase -> listOfTestCasesJSON.add(testcase.toJson()));
                }

                jsonResponse.put("testcases", listOfTestCasesJSON);
                jsonResponse.put("size", listOfTestCasesJSON.size());
                response.setContentType("application/json");
                response.getWriter().print(jsonResponse.toString());

            } catch (JSONException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

    private boolean isTestcaseContainingAllLabelsParam(TestCase testcase, List<Integer> labelIds) {
        for (Integer labelId : labelIds) {
            if(!isLabelIdPresentInListOfLabel(labelId, testcase.getLabels())) {
                return false;
            }
        }
        return true;
    }

    private boolean isLabelIdPresentInListOfLabel(Integer labelId, List<Label> labels) {
        boolean isPresent = false;
        for (Label label : labels) {
            if (label.getId().equals(labelId)) {
                isPresent = true;
            }
        }
        return isPresent;
    }

    /**
     * return a single value filter into the format of readByVarious() function
     *
     * @param param String to format
     * @return String[] with 1 element if param is defined, null otherwise
     */
    private String[] parseSingleValueFilter(String param) {
        if (param == null || param.isEmpty()) {
            return null;
        } else {
            String[] res = new String[1];
            res[0] = param;
            return res;
        }
    }

    /**
     * return a single value filter into the format of readByVarious() function
     *
     * @param param String to format
     * @return String[] with 1 element if param is defined, null otherwise
     */
    private String[] parseMultipleValuesFilter(String param) {
        if (param == null || param.isEmpty()) {
            return null;
        } else {
            return param.split(",");
        }
    }

    /**
     * convert a String[] to List<Integer>
     *
     * @param strings String[]
     * @return List<Integer>
     */
    private List<Integer> stringstoIntegerList(String[] strings) {
        if (strings == null || strings.length == 0) {
            return null;
        } else {
            List<Integer> res = new ArrayList<>();
            for (String str : strings) {
                res.add(Integer.parseInt(str));
            }
            return res;
        }
    }

}