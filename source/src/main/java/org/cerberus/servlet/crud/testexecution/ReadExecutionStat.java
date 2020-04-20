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
package org.cerberus.servlet.crud.testexecution;

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
import java.util.Iterator;
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
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.ITestCaseExecutionHttpStatService;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.har.entity.HarStat;
import org.cerberus.service.har.entity.HarStat.Types;
import org.cerberus.service.har.entity.HarStat.Units;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
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
    private IApplicationService applicationService;
    private ITestCaseService testCaseService;
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
     * @throws org.cerberus.exception.CerberusException
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
        List<String> system = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("system"), new ArrayList<String>(), "UTF8");
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
        String test = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("tests"), null, "UTF8");
        String testCase = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("testcases"), null, "UTF8");
        ltc.add(factoryTestCase.create(test, testCase));

        List<String> parties = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("parties"), Arrays.asList("total"), "UTF8");

        List<String> types = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("types"), Arrays.asList("total"), "UTF8");

        List<String> units = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("units"), Arrays.asList("request", "totalsize"), "UTF8");

        List<String> countries = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("countries"), new ArrayList<String>(), "UTF8");
        Boolean countriesDefined = (request.getParameterValues("countries") != null);

        List<String> environments = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("environments"), new ArrayList<String>(), "UTF8");
        Boolean environmentsDefined = (request.getParameterValues("environments") != null);

        List<String> robotDeclis = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("robotDeclis"), new ArrayList<String>(), "UTF8");
        Boolean robotDeclisDefined = (request.getParameterValues("robotDeclis") != null);

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem<JSONObject> answer = new AnswerItem<>(msg);

        // get all TestCase Execution Data.
        HashMap<String, Boolean> countryMap = new HashMap<>();
        HashMap<String, Boolean> environmentMap = new HashMap<>();
        HashMap<String, Boolean> robotDecliMap = new HashMap<>();
        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);

        List<TestCaseExecution> exeL = testCaseExecutionService.readByCriteria(null, null, null, null, ltc, fromD, toD);
        for (TestCaseExecution exe : exeL) {
            countryMap.put(exe.getCountry(), countries.contains(exe.getCountry()));
            environmentMap.put(exe.getEnvironment(), environments.contains(exe.getEnvironment()));
            robotDecliMap.put(exe.getRobotDecli(), robotDeclis.contains(exe.getRobotDecli()));
        }
        LOG.debug(countryMap);
        LOG.debug(environmentMap);
        LOG.debug(robotDecliMap);

        try {

            JSONObject jsonResponse1 = new JSONObject();
            answer = findExeStatList(appContext, request, exeL, countries, environments, robotDeclis);
            jsonResponse1 = (JSONObject) answer.getItem();

            JSONObject jsonResponse = new JSONObject();
            answer = findStatList(appContext, request, system, ltc, fromD, toD, parties, types, units, countryMap, countries, countriesDefined, environmentMap, environments, environmentsDefined, robotDecliMap, robotDeclis, robotDeclisDefined);
            jsonResponse = (JSONObject) answer.getItem();

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            jsonResponse.put("datasetExeTime", jsonResponse1.getJSONArray("curvesTime"));
            jsonResponse.put("datasetExeStatusNb", jsonResponse1.getJSONArray("curvesNb"));
            jsonResponse.put("datasetExeStatusNbDates", jsonResponse1.getJSONArray("curvesDatesNb"));

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private AnswerItem<JSONObject> findExeStatList(ApplicationContext appContext, HttpServletRequest request,
            List<TestCaseExecution> exeList,
            List<String> countries, List<String> environments, List<String> robotDeclis) throws JSONException {

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
                    && (robotDeclis.isEmpty() || robotDeclis.contains(exeCur.getRobotDecli()))) {

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

                    if (curveMap.containsKey(curveKey)) {
                        curArray = curveMap.get(curveKey);
                    } else {
                        curArray = new JSONArray();

                        curveObj = new JSONObject();
                        curveObj.put("key", curveKey);
                        TestCase a = factoryTestCase.create(exeCur.getTest(), exeCur.getTestCase());
                        try {
                            a = testCaseService.convert(testCaseService.readByKey(exeCur.getTest(), exeCur.getTestCase()));
                        } catch (CerberusException ex) {
                            LOG.error("Exception when getting TestCase details", ex);
                        }
                        curveObj.put("testcase", a.toJson());

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

    private AnswerItem<JSONObject> findStatList(ApplicationContext appContext, HttpServletRequest request,
            List<String> system, List<TestCase> ltc, Date from, Date to,
            List<String> parties, List<String> types, List<String> units,
            HashMap<String, Boolean> countryMap, List<String> countries, Boolean countriesDefined,
            HashMap<String, Boolean> environmentMap, List<String> environments, Boolean environmentsDefined,
            HashMap<String, Boolean> robotDecliMap, List<String> robotDeclis, Boolean robotDeclisDefined
    ) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        testCaseExecutionHttpStatService = appContext.getBean(ITestCaseExecutionHttpStatService.class);
        applicationService = appContext.getBean(IApplicationService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);

        HashMap<String, JSONArray> curveMap = new HashMap<>();
        HashMap<String, JSONObject> curveObjMap = new HashMap<>();

        // Filter Map
        HashMap<String, TestCase> testCaseMap = new HashMap<>();
        HashMap<String, Boolean> systemMap = new HashMap<>();
        HashMap<String, Boolean> applicationMap = new HashMap<>();
        // Indicator Map
        HashMap<String, Boolean> partyMap = new HashMap<>();
        partyMap.put("total", false);
        partyMap.put("internal", false);
        HashMap<String, Boolean> typeMap = new HashMap<>();
        HashMap<String, Boolean> unitMap = new HashMap<>();

        String curveKey;
        JSONArray curArray = new JSONArray();
        JSONObject curveObj = new JSONObject();
        JSONObject pointObj = new JSONObject();

        AnswerList<TestCaseExecutionHttpStat> resp = testCaseExecutionHttpStatService.readByCriteria("OK", ltc, from, to, system, countries, environments, robotDeclis);

        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestCaseExecutionHttpStat statCur : (List<TestCaseExecutionHttpStat>) resp.getDataList()) {

                // Get List of Third Party
                JSONObject partiesA = statCur.getStatDetail().getJSONObject("thirdparty");
                Iterator<String> jsonObjectIterator = partiesA.keys();
                jsonObjectIterator.forEachRemaining(key -> {
                    partyMap.put(key, false);
                });

                if (!countryMap.containsKey(statCur.getCountry())) {
                    countryMap.put(statCur.getCountry(), false);
                }
                if (!systemMap.containsKey(statCur.getSystem())) {
                    systemMap.put(statCur.getSystem(), false);
                }
                if (!applicationMap.containsKey(statCur.getApplication())) {
                    applicationMap.put(statCur.getApplication(), false);
                }
                if (!environmentMap.containsKey(statCur.getEnvironment())) {
                    environmentMap.put(statCur.getEnvironment(), false);
                }
                if (!robotDecliMap.containsKey(statCur.getRobotDecli())) {
                    robotDecliMap.put(statCur.getRobotDecli(), false);
                }

                for (String party : parties) {
                    for (String type : types) {
                        for (String unit : units) {
                            curveKey = getKeyCurve(statCur, party, type, unit);
                            int x = getValue(statCur, party, type, unit);
//                            LOG.debug("return : " + x);
                            if (x != -1) {
                                testCaseMap.put(statCur.getTest() + "/\\" + statCur.getTestCase(), factoryTestCase.create(statCur.getTest(), statCur.getTestCase()));
                                countryMap.put(statCur.getCountry(), true);
                                systemMap.put(statCur.getSystem(), true);
                                applicationMap.put(statCur.getApplication(), true);
                                environmentMap.put(statCur.getEnvironment(), true);
                                robotDecliMap.put(statCur.getRobotDecli(), true);
                                partyMap.put(party, true);
                                typeMap.put(type, true);
                                unitMap.put(unit, true);

                                pointObj = new JSONObject();
                                Date d = new Date(statCur.getStart().getTime());
                                TimeZone tz = TimeZone.getTimeZone("UTC");
                                DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                                df.setTimeZone(tz);
                                pointObj.put("x", df.format(d));

                                pointObj.put("y", x);
                                pointObj.put("exe", statCur.getId());
                                pointObj.put("exeControlStatus", statCur.getControlStatus());

                                if (curveMap.containsKey(curveKey)) {
                                    curArray = curveMap.get(curveKey);
                                } else {
                                    curArray = new JSONArray();

                                    curveObj = new JSONObject();
                                    curveObj.put("key", curveKey);
                                    TestCase a = factoryTestCase.create(statCur.getTest(), statCur.getTestCase());
                                    try {
                                        a = testCaseService.convert(testCaseService.readByKey(statCur.getTest(), statCur.getTestCase()));
                                    } catch (CerberusException ex) {
                                        LOG.error("Exception when getting TestCase details", ex);
                                    }
                                    curveObj.put("testcase", a.toJson());

                                    curveObj.put("country", statCur.getCountry());
                                    curveObj.put("environment", statCur.getEnvironment());
                                    curveObj.put("robotdecli", statCur.getRobotDecli());
                                    curveObj.put("system", statCur.getSystem());
                                    curveObj.put("application", statCur.getApplication());
                                    curveObj.put("unit", unit);
                                    curveObj.put("party", party);
                                    curveObj.put("type", type);

                                    curveObjMap.put(curveKey, curveObj);
                                }
                                curArray.put(pointObj);
                                curveMap.put(curveKey, curArray);

                            }

                        }
                    }

                }

            }
        }

        JSONArray curvesArray = new JSONArray();
        for (Map.Entry<String, JSONObject> entry : curveObjMap.entrySet()) {
            String key = entry.getKey();
            JSONObject val = entry.getValue();
            JSONObject localcur = new JSONObject();
            localcur.put("key", val);
            localcur.put("points", curveMap.get(key));
            curvesArray.put(localcur);
        }
        object.put("datasetPerf", curvesArray);

        JSONObject objectdist = getAllDistinct(countryMap, systemMap, testCaseMap, applicationMap, environmentMap, robotDecliMap, partyMap, typeMap, unitMap, parties, types, units, countriesDefined, environmentsDefined, robotDeclisDefined);
        object.put("distinct", objectdist);

        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    private JSONObject getAllDistinct(
            HashMap<String, Boolean> countryMap,
            HashMap<String, Boolean> systemMap,
            HashMap<String, TestCase> testCaseMap,
            HashMap<String, Boolean> applicationMap,
            HashMap<String, Boolean> environmentMap,
            HashMap<String, Boolean> robotDecliMap,
            HashMap<String, Boolean> partyMap,
            HashMap<String, Boolean> typeMap,
            HashMap<String, Boolean> unitMap,
            List<String> parties,
            List<String> types,
            List<String> units,
            Boolean countriesDefined,
            Boolean environmentsDefined,
            Boolean robotDeclisDefined) throws JSONException {

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
                TestCase a = testCaseService.convert(testCaseService.readByKey(tc.getTest(), tc.getTestCase()));
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
        for (Units v : HarStat.Units.values()) {
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", v.name().toLowerCase());
            objectcount.put("hasData", unitMap.containsKey(v.name().toLowerCase()));
            objectcount.put("isRequested", units.contains(v.name().toLowerCase()));
            objectdinst.put(objectcount);
        }
        objectdist.put("units", objectdinst);

        objectdinst = new JSONArray();
        for (Types v : HarStat.Types.values()) {
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", v.name().toLowerCase());
            objectcount.put("hasData", typeMap.containsKey(v.name().toLowerCase()));
            objectcount.put("isRequested", types.contains(v.name().toLowerCase()));
            objectdinst.put(objectcount);
        }
        objectdist.put("types", objectdinst);

        objectdinst = new JSONArray();
        for (Map.Entry<String, Boolean> entry : partyMap.entrySet()) {
            String key = entry.getKey();
            Boolean val = entry.getValue();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectcount.put("hasData", val);
            objectcount.put("isRequested", parties.contains(key));
            objectdinst.put(objectcount);
        }
        objectdist.put("parties", objectdinst);

        return objectdist;
    }

    private int getValue(TestCaseExecutionHttpStat stat, String party, String type, String unit) {
//        LOG.debug("Start : " + stat.getId() + " | " + party + " - " + type + " - " + unit);
        try {
            switch (party) {
                case "internal":
                    switch (type) {
                        case "content":
                        case "css":
                        case "font":
                        case "html":
                        case "img":
                        case "js":
                        case "media":
                        case "other":
                            switch (unit) {
                                case "request":
                                    return stat.getStatDetail().getJSONObject("internal").getJSONObject("type").getJSONObject(type).getInt("requests");
                                case "totalsize":
                                    return stat.getStatDetail().getJSONObject("internal").getJSONObject("type").getJSONObject(type).getInt("sizeSum");
                                case "sizemax":
                                    return stat.getStatDetail().getJSONObject("internal").getJSONObject("type").getJSONObject(type).getInt("sizeMax");
                                default:
                                    break;
                            }
                            break;
                        case "total":
                            switch (unit) {
                                case "request":
                                    return stat.getInternal_hits();
                                case "totalsize":
                                    return stat.getInternal_size();
                                case "totaltime":
                                    return stat.getInternal_time();
                                case "timemax":
                                    return stat.getStatDetail().getJSONObject("internal").getJSONObject("time").getInt("max");
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "total":
                    switch (type) {
                        case "content":
                        case "font":
                        case "other":
                            switch (unit) {
                                case "request":
                                    return stat.getStatDetail().getJSONObject("total").getJSONObject("type").getJSONObject(type).getInt("requests");
                                case "totalsize":
                                    return stat.getStatDetail().getJSONObject("total").getJSONObject("type").getJSONObject(type).getInt("sizeSum");
                                case "sizemax":
                                    return stat.getStatDetail().getJSONObject("total").getJSONObject("type").getJSONObject(type).getInt("sizeMax");
                                default:
                                    break;
                            }
                            break;
                        case "img":
                            switch (unit) {
                                case "request":
                                    return stat.getImg_hits();
                                case "totalsize":
                                    return stat.getImg_size();
                                case "sizemax":
                                    return stat.getImg_size_max();
                                default:
                                    break;
                            }
                            break;
                        case "js":
                            switch (unit) {
                                case "request":
                                    return stat.getJs_hits();
                                case "totalsize":
                                    return stat.getJs_size();
                                case "sizemax":
                                    return stat.getJs_size_max();
                                default:
                                    break;
                            }
                            break;
                        case "css":
                            switch (unit) {
                                case "request":
                                    return stat.getCss_hits();
                                case "totalsize":
                                    return stat.getCss_size();
                                case "sizemax":
                                    return stat.getCss_size_max();
                                default:
                                    break;
                            }
                            break;
                        case "html":
                            switch (unit) {
                                case "request":
                                    return stat.getHtml_hits();
                                case "totalsize":
                                    return stat.getHtml_size();
                                case "sizemax":
                                    return stat.getHtml_size_max();
                                default:
                                    break;
                            }
                            break;
                        case "media":
                            switch (unit) {
                                case "request":
                                    return stat.getMedia_hits();
                                case "totalsize":
                                    return stat.getMedia_size();
                                case "sizemax":
                                    return stat.getMedia_size_max();
                                default:
                                    break;
                            }
                            break;
                        case "total":
                            switch (unit) {
                                case "request":
                                    return stat.getTotal_hits();
                                case "totalsize":
                                    return stat.getTotal_size();
                                case "totaltime":
                                    return stat.getTotal_time();
                                case "timemax":
                                    return stat.getStatDetail().getJSONObject("total").getJSONObject("time").getInt("max");
                                case "nbthirdparty":
                                    return stat.getStatDetail().getInt("nbThirdParty");
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default: // For Third Paries.
                    switch (type) {
                        case "content":
                        case "css":
                        case "font":
                        case "html":
                        case "img":
                        case "js":
                        case "media":
                        case "other":
                            switch (unit) {
                                case "request":
                                    LOG.debug("Result.");
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("type").getJSONObject(type).getInt("requests");
                                case "totalsize":
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("type").getJSONObject(type).getInt("sizeSum");
                                case "sizemax":
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("type").getJSONObject(type).getInt("sizeMax");
                                default:
                                    break;
                            }
                            break;
                        case "total":
                            switch (unit) {
                                case "request":
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("requests").getInt("nb");
                                case "totalsize":
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("size").getInt("sum");
                                case "sizemax":
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("size").getInt("max");
                                case "totaltime":
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("time").getInt("totalDuration");
                                case "timemax":
                                    return stat.getStatDetail().getJSONObject("thirdparty").getJSONObject(party).getJSONObject("time").getInt("max");
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
            }
            return -1;
        } catch (JSONException ex) {
            LOG.debug("Start : " + stat.getId() + " | " + party + " - " + type + " - " + unit, ex);
            return -1;
        }
    }

    private JSONObject convertApplicationToJSONObject(Application app) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(app));
        return result;
    }

    private String getKeyCurve(TestCaseExecutionHttpStat stat, String party, String type, String unit) {
        return type + "/" + party + "/" + unit + "/" + stat.getTest() + "/" + stat.getTestCase() + "/" + stat.getCountry() + "/" + stat.getEnvironment() + "/" + stat.getRobotDecli() + "/" + stat.getSystem() + "/" + stat.getApplication();
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
