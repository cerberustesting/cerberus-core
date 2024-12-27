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
package org.cerberus.core.servlet.crud.testexecution;

import com.google.gson.Gson;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ITestCaseExecutionHttpStatService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author vertigo
 */
@WebServlet(name = "ReadExecutionStat", urlPatterns = {"/ReadExecutionStat"})
public class ReadExecutionStat extends HttpServlet {

    private ITestCaseExecutionHttpStatService testCaseExecutionHttpStatService;
    private IFactoryTestCase factoryTestCase;
    private ITestCaseService testCaseService;
    private IApplicationService applicationService;
    private ITestCaseExecutionService testCaseExecutionService;

    private static final Logger LOG = LogManager.getLogger(ReadExecutionStat.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
    private static final String DATE_FORMAT_DAY = "yyyy-MM-dd";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.core.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        String echo = request.getParameter("sEcho");
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        /**
         * Parsing and securing all required parameters.
         */
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);
        List<String> system = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("system"), new ArrayList<>(), "UTF8");
        String from = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("from"), null, "UTF8");
        String to = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("to"), null, "UTF8");

        LOG.debug("from : " + from);
        LOG.debug("to : " + to);
        Date fromD;
        try {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(tz);
            fromD = df.parse(from);
        } catch (ParseException ex) {
            fromD = Date.from(ZonedDateTime.now().minusMonths(1).toInstant());
            LOG.debug("Exception when parsing date", ex);
        }
        Date toD;
        try {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(tz);
            toD = df.parse(to);
        } catch (ParseException ex) {
            toD = Date.from(ZonedDateTime.now().toInstant());
            LOG.debug("Exception when parsing date", ex);
        }
        LOG.debug("from : " + fromD);
        LOG.debug("to : " + toD);

        List<TestCase> ltc = new ArrayList<>();
        List<String> test = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("tests"), new ArrayList<>(), "UTF8");
        List<String> testCase = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("testcases"), new ArrayList<>(), "UTF8");
        int i = 0;
        for (String string : test) {
            ltc.add(factoryTestCase.create(string, testCase.get(i++)));
        }
        if (ltc.size() <= 0) {
            ltc.add(factoryTestCase.create("", ""));
        }

        List<String> parties = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("parties"), Arrays.asList("total"), "UTF8");

        List<String> types = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("types"), Arrays.asList("total"), "UTF8");

        List<String> units = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("units"), Arrays.asList("request", "totalsize"), "UTF8");

        List<String> countries = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("countries"), new ArrayList<>(), "UTF8");
        Boolean countriesDefined = (request.getParameterValues("countries") != null);

        List<String> environments = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("environments"), new ArrayList<>(), "UTF8");
        Boolean environmentsDefined = (request.getParameterValues("environments") != null);

        List<String> robotDeclis = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("robotDeclis"), new ArrayList<>(), "UTF8");
        Boolean robotDeclisDefined = (request.getParameterValues("robotDeclis") != null);

        List<String> controlStatuss = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("controlStatuss"), new ArrayList<>(), "UTF8");
        Boolean controlStatussDefined = (request.getParameterValues("controlStatuss") != null);

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem<JSONObject> answer = new AnswerItem<>(msg);

        // Map of countries, Environments and RobotDeclis (Boolean contains true if requested to be displayed).
        HashMap<String, Boolean> countryMap = new HashMap<>();
        HashMap<String, Boolean> environmentMap = new HashMap<>();
        HashMap<String, Boolean> robotDecliMap = new HashMap<>();
        HashMap<String, Boolean> controlStatusMap = new HashMap<>();
        HashMap<String, Boolean> systemMap = new HashMap<>();
        HashMap<String, TestCase> testCaseMap = new HashMap<>();
        HashMap<String, Boolean> applicationMap = new HashMap<>();

        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        List<TestCaseExecution> exeL = testCaseExecutionService.readByCriteria(null, null, null, null, ltc, fromD, toD);
        for (TestCaseExecution exe : exeL) {
            countryMap.put(exe.getCountry(), countries.contains(exe.getCountry()));
            environmentMap.put(exe.getEnvironment(), environments.contains(exe.getEnvironment()));
            robotDecliMap.put(exe.getRobotDecli(), robotDeclis.contains(exe.getRobotDecli()));
            controlStatusMap.put(exe.getControlStatus(), controlStatuss.contains(exe.getControlStatus()));

            testCaseMap.put(exe.getTest() + "/\\" + exe.getTestCase(), factoryTestCase.create(exe.getTest(), exe.getTestCase()));
            systemMap.put(exe.getSystem(), true);
            applicationMap.put(exe.getApplication(), true);

        }
        LOG.debug(countryMap);
        LOG.debug(environmentMap);
        LOG.debug(robotDecliMap);
        LOG.debug(controlStatusMap);

        try {

            JSONObject jsonResponse = new JSONObject();
            testCaseExecutionHttpStatService = appContext.getBean(ITestCaseExecutionHttpStatService.class);
            answer = testCaseExecutionHttpStatService.readByCriteria(null, ltc, fromD, toD, system, countries, environments, robotDeclis, parties, types, units);
            jsonResponse = answer.getItem();

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            JSONObject jsonResponse1 = new JSONObject();
            answer = findExeStatList(appContext, request, exeL, countries, environments, robotDeclis, controlStatuss);
            jsonResponse1 = answer.getItem();

            jsonResponse.put("datasetExeTime", jsonResponse1.getJSONArray("curvesTime"));
            jsonResponse.put("datasetExeStatusNb", jsonResponse1.getJSONArray("curvesNb"));
            jsonResponse.put("datasetExeStatusNbDates", jsonResponse1.getJSONArray("curvesDatesNb"));

            JSONObject objectdist = getAllDistinct(countryMap, systemMap, testCaseMap, applicationMap, environmentMap, robotDecliMap, controlStatusMap, countriesDefined, environmentsDefined, robotDeclisDefined, controlStatussDefined);
            objectdist.put("units", jsonResponse.getJSONArray("distinctUnits"));
            objectdist.put("types", jsonResponse.getJSONArray("distinctTypes"));
            objectdist.put("parties", jsonResponse.getJSONArray("distinctParties"));
            jsonResponse.remove("distinctUnits");
            jsonResponse.remove("distinctTypes");
            jsonResponse.remove("distinctParties");
            jsonResponse.put("distinct", objectdist);

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private AnswerItem<JSONObject> findExeStatList(ApplicationContext appContext, HttpServletRequest request,
            List<TestCaseExecution> exeList,
            List<String> countries, List<String> environments, List<String> robotDeclis, List<String> controlStatuss) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        testCaseExecutionHttpStatService = appContext.getBean(ITestCaseExecutionHttpStatService.class);
        applicationService = appContext.getBean(IApplicationService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);

        HashMap<String, JSONArray> curveMap = new HashMap<>();
        HashMap<String, JSONObject> curveObjMap = new HashMap<>();

        String curveKey = "";
        JSONArray curArray = new JSONArray();
        JSONObject curveObj = new JSONObject();
        JSONObject pointObj = new JSONObject();

        HashMap<String, JSONObject> curveStatusObjMap = new HashMap<>();
        HashMap<String, Boolean> curveDateMap = new HashMap<>();
        HashMap<String, Integer> curveDateStatusMap = new HashMap<>();

        String curveKeyStatus = "";
        JSONObject curveStatObj = new JSONObject();

        for (TestCaseExecution exeCur : exeList) {

            if ((countries.isEmpty() || countries.contains(exeCur.getCountry()))
                    && (environments.isEmpty() || environments.contains(exeCur.getEnvironment()))
                    && (robotDeclis.isEmpty() || robotDeclis.contains(exeCur.getRobotDecli()))
                    && (controlStatuss.isEmpty() || controlStatuss.contains(exeCur.getControlStatus()))) {

                /**
                 * Curves of testcase response time.
                 */
                curveKey = exeCur.getTest() + "|" + exeCur.getTestCase() + "|" + exeCur.getCountry() + "|" + exeCur.getEnvironment() + "|" + exeCur.getRobotDecli() + "|";

                long y = 0;
                if (!exeCur.getControlStatus().equalsIgnoreCase("PE")) {
                    y = exeCur.getEnd() - exeCur.getStart();

                    pointObj = new JSONObject();
                    Date d = new Date(exeCur.getStart());
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                    df.setTimeZone(tz);
                    pointObj.put("x", df.format(d));

                    pointObj.put("y", y);
                    pointObj.put("exe", exeCur.getId());
                    pointObj.put("exeControlStatus", exeCur.getControlStatus());
                    pointObj.put("falseNegative", exeCur.isFalseNegative());

                    if (curveMap.containsKey(curveKey)) {
                        curArray = curveMap.get(curveKey);
                    } else {
                        curArray = new JSONArray();

                        curveObj = new JSONObject();
                        curveObj.put("key", curveKey);
                        TestCase a = factoryTestCase.create(exeCur.getTest(), exeCur.getTestCase());
                        try {
                            a = testCaseService.convert(testCaseService.readByKey(exeCur.getTest(), exeCur.getTestCase()));
                            curveObj.put("testcase", a.toJson());
                        } catch (CerberusException ex) {
                            LOG.error("Exception when getting TestCase details", ex);
                        }

                        curveObj.put("country", exeCur.getCountry());
                        curveObj.put("environment", exeCur.getEnvironment());
                        curveObj.put("robotdecli", exeCur.getRobotDecli());
                        curveObj.put("system", exeCur.getSystem());
                        curveObj.put("application", exeCur.getApplication());
                        curveObj.put("unit", "testduration");

                        curveObjMap.put(curveKey, curveObj);
                    }
                    curArray.put(pointObj);
                    curveMap.put(curveKey, curArray);
                }

                /**
                 * Bar Charts per control status.
                 */
                curveKeyStatus = exeCur.getControlStatus();

                Date d = new Date(exeCur.getStart());
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat(DATE_FORMAT_DAY);
                df.setTimeZone(tz);
                String dday = df.format(d);

                curveDateMap.put(dday, false);

                String keyDateStatus = curveKeyStatus + "-" + dday;
                if (curveDateStatusMap.containsKey(keyDateStatus)) {
                    curveDateStatusMap.put(keyDateStatus, curveDateStatusMap.get(keyDateStatus) + 1);
                } else {
                    curveDateStatusMap.put(keyDateStatus, 1);
                }

                if (!curveStatusObjMap.containsKey(curveKeyStatus)) {

                    curveStatObj = new JSONObject();
                    curveStatObj.put("key", curveKeyStatus);
                    curveStatObj.put("unit", "nbExe");

                    curveStatusObjMap.put(curveKeyStatus, curveStatObj);
                }

            }

        }

        /**
         * Feed Curves of testcase response time to JSON.
         */
        JSONArray curvesArray = new JSONArray();
        for (Map.Entry<String, JSONObject> entry : curveObjMap.entrySet()) {
            String key = entry.getKey();
            JSONObject val = entry.getValue();
            JSONObject localcur = new JSONObject();
            localcur.put("key", val);
            localcur.put("points", curveMap.get(key));
            curvesArray.put(localcur);
        }
        object.put("curvesTime", curvesArray);

        /**
         * Bar Charts per control status to JSON.
         */
        curvesArray = new JSONArray();
        for (Map.Entry<String, Boolean> entry : curveDateMap.entrySet()) {
            curvesArray.put(entry.getKey());
        }
        object.put("curvesDatesNb", curvesArray);

        curvesArray = new JSONArray();
        for (Map.Entry<String, JSONObject> entry : curveStatusObjMap.entrySet()) {
            String key = entry.getKey();
            JSONObject val = entry.getValue();

            JSONArray valArray = new JSONArray();

            for (Map.Entry<String, Boolean> entry1 : curveDateMap.entrySet()) {
                String key1 = entry1.getKey(); // Date
                if (curveDateStatusMap.containsKey(key + "-" + key1)) {
                    valArray.put(curveDateStatusMap.get(key + "-" + key1));
                } else {
                    valArray.put(0);
                }
            }

            JSONObject localcur = new JSONObject();
            localcur.put("key", val);
            localcur.put("points", valArray);
            curvesArray.put(localcur);
        }
        object.put("curvesNb", curvesArray);

        item.setItem(object);
        return item;
    }

    private JSONObject getAllDistinct(
            HashMap<String, Boolean> countryMap,
            HashMap<String, Boolean> systemMap,
            HashMap<String, TestCase> testCaseMap,
            HashMap<String, Boolean> applicationMap,
            HashMap<String, Boolean> environmentMap,
            HashMap<String, Boolean> robotDecliMap,
            HashMap<String, Boolean> controlStatusMap,
            Boolean countriesDefined,
            Boolean environmentsDefined,
            Boolean robotDeclisDefined,Boolean controlStatussDefined) throws JSONException {

        JSONObject objectdist = new JSONObject();

        JSONArray objectSdinst = new JSONArray();
        for (Map.Entry<String, Boolean> sys : systemMap.entrySet()) {
            String key = sys.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectcount.put("hasData", systemMap.containsKey(key));
            objectSdinst.put(objectcount);
        }
        objectdist.put("systems", objectSdinst);

        JSONArray objectTCdinst = new JSONArray();
        for (Map.Entry<String, TestCase> env : testCaseMap.entrySet()) {
            try {
                String key = env.getKey();
                TestCase tc = env.getValue();
                TestCase a = testCaseService.convert(testCaseService.readByKey(tc.getTest(), tc.getTestcase()));
                objectTCdinst.put(a.toJson());
            } catch (CerberusException ex) {
                LOG.error("Exception when getting TestCase.", ex);
            }
        }
        objectdist.put("testCases", objectTCdinst);

        JSONArray objectAdinst = new JSONArray();
        for (Map.Entry<String, Boolean> app : applicationMap.entrySet()) {
            try {
                String key = app.getKey();
                Application a = applicationService.convert(applicationService.readByKey(key));
                objectAdinst.put(convertApplicationToJSONObject(a));
            } catch (CerberusException ex) {
                LOG.error("Exception when getting Application.", ex);
            }
        }
        objectdist.put("applications", objectAdinst);

        JSONArray objectCdinst = new JSONArray();
        for (Map.Entry<String, Boolean> country : countryMap.entrySet()) {
            String key = country.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectcount.put("hasData", countryMap.containsKey(key));
            if (countriesDefined) {
                objectcount.put("isRequested", countryMap.get(key));
            } else {
                objectcount.put("isRequested", true);
            }
            objectCdinst.put(objectcount);
        }
        objectdist.put("countries", objectCdinst);

        JSONArray objectdinst = new JSONArray();
        for (Map.Entry<String, Boolean> env : environmentMap.entrySet()) {
            String key = env.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectcount.put("hasData", environmentMap.containsKey(key));
            if (environmentsDefined) {
                objectcount.put("isRequested", environmentMap.get(key));
            } else {
                objectcount.put("isRequested", true);
            }
            objectdinst.put(objectcount);
        }
        objectdist.put("environments", objectdinst);

        objectdinst = new JSONArray();
        for (Map.Entry<String, Boolean> env : robotDecliMap.entrySet()) {
            String key = env.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectcount.put("hasData", robotDecliMap.containsKey(key));
            if (robotDeclisDefined) {
                objectcount.put("isRequested", robotDecliMap.get(key));
            } else {
                objectcount.put("isRequested", true);
            }
            objectdinst.put(objectcount);
        }
        objectdist.put("robotDeclis", objectdinst);

        objectdinst = new JSONArray();
        for (Map.Entry<String, Boolean> env : controlStatusMap.entrySet()) {
            String key = env.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectcount.put("hasData", controlStatusMap.containsKey(key));
            if (controlStatussDefined) {
                objectcount.put("isRequested", controlStatusMap.get(key));
            } else {
                objectcount.put("isRequested", true);
            }
            objectdinst.put(objectcount);
        }
        objectdist.put("controlStatuss", objectdinst);

        return objectdist;
    }

    private JSONObject convertApplicationToJSONObject(Application app) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(app));
        return result;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
