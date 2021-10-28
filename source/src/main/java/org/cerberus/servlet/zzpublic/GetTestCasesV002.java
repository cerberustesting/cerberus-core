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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.service.authentification.IAPIKeyService;
import org.cerberus.util.ParameterParserUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author Nouxx
 * 
 */
@WebServlet(name = "GetTestCasesV002", urlPatterns = { "/GetTestCasesV002" })
public class GetTestCasesV002 extends HttpServlet {

    private ITestCaseService testCaseService;
    private IAPIKeyService apiKeyService;

    private static final Logger LOG = LogManager.getLogger("GetTestCasesV002");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        // currently, the servlet only supports filtering on a single Application
        String Application = ParameterParserUtil.parseStringParam(request.getParameter("Application"), "");
        // currently, the servlet only supports filtering on a single Type
        String Type = ParameterParserUtil.parseStringParam(request.getParameter("Type"), "");
        // currently, the servlet only supports filtering on a single Priority
        String Priority = ParameterParserUtil.parseStringParam(request.getParameter("Priority"), "");
        // the servlet supports filtering on several labels id (AND operator)
        String LabelsId = ParameterParserUtil.parseStringParam(request.getParameter("Labels"), "");
        // the servlet supports filtering on several status (OR operator)
        String Statuses = ParameterParserUtil.parseStringParam(request.getParameter("Statuses"), "");

        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createForPublicCalls("/GetTestCasesV002", "CALL",
                "GetTestCasesV002 called : " + request.getRequestURL(), request);

        apiKeyService = appContext.getBean(IAPIKeyService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);

        if (apiKeyService.authenticate(request, response)) {
            AnswerList<TestCase> AnswerlistOfTestCases;
            List<JSONObject> listOfTestCasesJSON = new ArrayList<>();
            try {
                // build JSON response object
                JSONObject jsonResponse = new JSONObject();

                // ignored parameters
                String[] searchTerm_Test = null;
                String[] searchTerm_creator = null;
                String[] searchTerm_implementer = null;
                String[] searchTerm_system = null;
                String[] searchTerm_campaign = null;

                // currently only handling a single application
                String[] searchTerm_Application = parseSingleValueFilter(Application);
                // currently handling a single priority
                String[] searchTerm_priority = parseSingleValueFilter(Priority);
                // currently handling a single type
                String[] searchTerm_type = parseSingleValueFilter(Type);

                List<Integer> searchTerm_labelids = StringstoIntegerList(parseMultipleValuesFilter(LabelsId));

                String[] searchTerm_status = parseMultipleValuesFilter(Statuses);

                AnswerlistOfTestCases = testCaseService.readByVarious(searchTerm_Test, searchTerm_Application,
                        searchTerm_creator, searchTerm_implementer, searchTerm_system, searchTerm_campaign,
                        searchTerm_labelids, searchTerm_priority, searchTerm_type, searchTerm_status, -1);

                List<TestCase> listOfTestCases = AnswerlistOfTestCases.getDataList();

                for (int i = 0; i < listOfTestCases.size(); i++) {
                    TestCase testcase = listOfTestCases.get(i);
                    JSONObject testcaseJSON = testcase.toJson();
                    listOfTestCasesJSON.add(testcaseJSON);
                }

                //TODO: filter out the results on labels (AND instead of OR)

                jsonResponse.put("testcases", listOfTestCasesJSON);
                jsonResponse.put("size", listOfTestCases.size());
                response.setContentType("application/json");
                response.getWriter().print(jsonResponse.toString());

            } catch (JSONException ex) {
                LOG.error(ex.getMessage());
            }
        }
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
    private List<Integer> StringstoIntegerList(String[] strings) {
        if (strings == null || strings.length == 0) {
            return null;
        } else {
            List<Integer> res = new ArrayList<Integer>();
            for (int i = 0; i < strings.length; i++) {
                res.add(Integer.parseInt(strings[i]));
            }
            return res;
        }
    }

}
