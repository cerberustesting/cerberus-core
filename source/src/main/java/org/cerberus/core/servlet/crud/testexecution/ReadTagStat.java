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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.ITestCaseExecutionHttpStatService;
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
@WebServlet(name = "ReadTagStat", urlPatterns = {"/ReadTagStat"})
public class ReadTagStat extends HttpServlet {

    private ITestCaseExecutionHttpStatService testCaseExecutionHttpStatService;
    private IFactoryTestCase factoryTestCase;
    private IApplicationService applicationService;
    private ITestCaseService testCaseService;
    private ITagService tagService;

    private static final Logger LOG = LogManager.getLogger(ReadTagStat.class);
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
        List<String> system = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("system"), new ArrayList<>(), "UTF8");
        String from = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("from"), "2020-01-01T01:01:01.001Z", "UTF8");
        String to = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("to"), "2020-06-01T01:01:01.001Z", "UTF8");

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

        List<String> gp1 = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("group1s"), new ArrayList<>(), "UTF8");
        List<String> gp2 = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("group2s"), new ArrayList<>(), "UTF8");
        List<String> gp3 = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("group3s"), new ArrayList<>(), "UTF8");

        List<String> defaultCampaigns = new ArrayList<>();
        if (gp1.isEmpty() && gp2.isEmpty() && gp3.isEmpty()) {
            // If none of the groups are defined we allow not to filter per campaign.
            defaultCampaigns.add("");
        }
        List<String> campaigns = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("campaigns"), defaultCampaigns, "UTF8");

        List<String> countries = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("countries"), new ArrayList<>(), "UTF8");
        Boolean countriesDefined = (request.getParameterValues("countries") != null);
        LOG.debug("countries : " + countries);

        List<String> environments = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("environments"), new ArrayList<>(), "UTF8");
        Boolean environmentsDefined = (request.getParameterValues("environments") != null);
        LOG.debug("environments : " + environments);

        List<String> robotDeclis = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("robotDeclis"), new ArrayList<>(), "UTF8");
        Boolean robotDeclisDefined = (request.getParameterValues("robotDeclis") != null);
        LOG.debug("robotDeclis : " + robotDeclis);

        List<String> ciResults = ParameterParserUtil.parseListParamAndSanitize(request.getParameterValues("ciResults"), new ArrayList<>(), "UTF8");
        Boolean ciResultsDefined = (request.getParameterValues("ciResults") != null);
        LOG.debug("ciResults : " + ciResults);

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem<JSONObject> answer = new AnswerItem<>(msg);

        // get all TestCase Execution Data.
        HashMap<String, Boolean> countryMap = new HashMap<>();
        HashMap<String, Boolean> environmentMap = new HashMap<>();
        HashMap<String, Boolean> robotDecliMap = new HashMap<>();
        HashMap<String, Boolean> ciResultMap = new HashMap<>();
        tagService = appContext.getBean(ITagService.class);

//        List<TestCaseExecution> exeL = testCaseExecutionService.readByCriteria(null, null, null, null, ltc, fromD, toD);
        List<Tag> tagExeL = tagService.convert(tagService.readByVarious(campaigns, gp1, gp2, gp3, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), fromD, toD));
        for (Tag tagExe : tagExeL) {
            if (tagExe.getCountryList() != null) {
                countryMap.put(tagExe.getCountryList(), countries.contains(formatedJSONArray(tagExe.getCountryList())));
            }
            if (tagExe.getEnvironmentList() != null) {
                environmentMap.put(tagExe.getEnvironmentList(), environments.contains(formatedJSONArray(tagExe.getEnvironmentList())));
            }
            if (tagExe.getRobotDecliList() != null) {
                robotDecliMap.put(tagExe.getRobotDecliList(), robotDeclis.contains(formatedJSONArray(tagExe.getRobotDecliList())));
            }
            if (tagExe.getCiResult() != null) {
                ciResultMap.put(tagExe.getCiResult(), ciResults.contains(formatedJSONArray(tagExe.getCiResult())));
            }
        }
        LOG.debug(countryMap);
        LOG.debug(environmentMap);
        LOG.debug(robotDecliMap);
        LOG.debug(robotDecliMap);

        try {

            JSONObject jsonResponse = new JSONObject();
            answer = findExeStatList(appContext, request, tagExeL,
                    countryMap, countries, countriesDefined, environmentMap, environments, environmentsDefined, robotDecliMap, robotDeclis, robotDeclisDefined, ciResultMap, ciResults, ciResultsDefined);
            jsonResponse = answer.getItem();

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private AnswerItem<JSONObject> findExeStatList(ApplicationContext appContext, HttpServletRequest request,
            List<Tag> tagExeList,
            HashMap<String, Boolean> countryMap, List<String> countries, Boolean countriesDefined,
            HashMap<String, Boolean> environmentMap, List<String> environments, Boolean environmentsDefined,
            HashMap<String, Boolean> robotDecliMap, List<String> robotDeclis, Boolean robotDeclisDefined,
            HashMap<String, Boolean> ciResultMap, List<String> ciResults, Boolean ciResultsDefined
    ) throws JSONException {

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        msg.setDescription(msg.getDescription().replace("%ITEM%", "Tag").replace("%OPERATION%", "Read"));
        AnswerItem<JSONObject> item = new AnswerItem<>(msg);
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
        List<String> curveBarMap = new ArrayList<>();
        HashMap<String, Tag> curveTagObjMap = new HashMap<>();
        HashMap<String, List<Integer>> curveTagObjValMap = new HashMap<>();
        HashMap<String, Integer> curveTagObjValTotMap = new HashMap<>();

        String curveKeyStatus = "";
        JSONObject curveStatObj = new JSONObject();

        for (Tag exeCur : tagExeList) {

            if ((countries.isEmpty() || countries.contains(formatedJSONArray(exeCur.getCountryList())))
                    && (environments.isEmpty() || environments.contains(formatedJSONArray(exeCur.getEnvironmentList())))
                    && (robotDeclis.isEmpty() || robotDeclis.contains(formatedJSONArray(exeCur.getRobotDecliList())))
                    && (ciResults.isEmpty() || ciResults.contains(exeCur.getCiResult()))) {

                /**
                 * Curves of tag response time.
                 */
                curveKey = exeCur.getCampaign() + "|" + exeCur.getCountryList() + "|" + exeCur.getEnvironmentList() + "|" + exeCur.getRobotDecliList() + "|";

                long y = 0;
                long ymin = 0;
                y = exeCur.getDateEndQueue().getTime() - exeCur.getDateStartExe().getTime();
                ymin = y / 60000;

                if (!countryMap.containsKey(exeCur.getCountryList())) {
                    countryMap.put(exeCur.getCountryList(), false);
                }
                if (!environmentMap.containsKey(exeCur.getEnvironmentList())) {
                    environmentMap.put(exeCur.getEnvironmentList(), false);
                }
                if (!robotDecliMap.containsKey(exeCur.getRobotDecliList())) {
                    robotDecliMap.put(exeCur.getRobotDecliList(), false);
                }

                if (y > 0) {

                    countryMap.put(exeCur.getCountryList(), true);
                    environmentMap.put(exeCur.getEnvironmentList(), true);
                    robotDecliMap.put(exeCur.getRobotDecliList(), true);

                    pointObj = new JSONObject();
                    Date d = new Date(exeCur.getDateStartExe().getTime());
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                    df.setTimeZone(tz);
                    pointObj.put("x", df.format(d));

                    pointObj.put("y", ymin);
                    pointObj.put("tag", exeCur.getTag());
                    pointObj.put("falseNegative", exeCur.isFalseNegative());
                    pointObj.put("ciRes", exeCur.getCiResult());
                    pointObj.put("ciSc", exeCur.getCiScore());
                    pointObj.put("ciScT", exeCur.getCiScoreThreshold());
                    pointObj.put("ciScM", exeCur.getCiScoreMax());
                    pointObj.put("nbFlaky", exeCur.getNbFlaky());
                    pointObj.put("nbMuted", exeCur.getNbMuted());
                    pointObj.put("nbExe", exeCur.getNbExe());
                    pointObj.put("nbExeU", exeCur.getNbExeUsefull());
                    pointObj.put("description", exeCur.getDescription());
                    pointObj.put("comment", exeCur.getComment());

                    if (curveMap.containsKey(curveKey)) {
                        curArray = curveMap.get(curveKey);
                    } else {
                        curArray = new JSONArray();

                        curveObj = new JSONObject();
                        curveObj.put("key", curveKey);
                        curveObj.put("campaign", exeCur.getCampaign());

                        curveObj.put("country", formatedJSONArray(exeCur.getCountryList()));
                        curveObj.put("environment", formatedJSONArray(exeCur.getEnvironmentList()));
                        curveObj.put("robotdecli", formatedJSONArray(exeCur.getRobotDecliList()));
                        curveObj.put("system", formatedJSONArray(exeCur.getSystemList()));
                        curveObj.put("application", formatedJSONArray(exeCur.getApplicationList()));
                        curveObj.put("unit", "duration");

                        curveObjMap.put(curveKey, curveObj);
                    }
                    curArray.put(pointObj);
                    curveMap.put(curveKey, curArray);
                }

                /**
                 * Bar Charts per control status.
                 */
                curveBarMap.add(exeCur.getTag());
                curveTagObjMap.put(exeCur.getTag(), exeCur);

                curveKeyStatus = "RETRY";
                List<Integer> tempList;
                Integer myVal = exeCur.getNbExe() + exeCur.getNbQE() + exeCur.getNbQU() - exeCur.getNbExeUsefull();
                if (curveTagObjValMap.containsKey(curveKeyStatus)) {
                    tempList = curveTagObjValMap.get(curveKeyStatus);
                    tempList.add(myVal);
                } else {
                    tempList = new ArrayList<>();
                    tempList.add(myVal);
                }
                curveTagObjValMap.put(curveKeyStatus, tempList);
                if (curveTagObjValTotMap.containsKey(curveKeyStatus)) {
                    curveTagObjValTotMap.put(curveKeyStatus, curveTagObjValTotMap.get(curveKeyStatus) + myVal);
                } else {
                    curveTagObjValTotMap.put(curveKeyStatus, myVal);
                }

                for (TestCaseExecution.ControlStatus ctrlStat : TestCaseExecution.ControlStatus.values()) {
                    curveKeyStatus = ctrlStat.name();

                    myVal = 0;
                    switch (curveKeyStatus) {
                        case TestCaseExecution.CONTROLSTATUS_CA:
                            myVal = exeCur.getNbCA();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_FA:
                            myVal = exeCur.getNbFA();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_KO:
                            myVal = exeCur.getNbKO();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_NA:
                            myVal = exeCur.getNbNA();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_NE:
                            myVal = exeCur.getNbNE();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_OK:
                            myVal = exeCur.getNbOK();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_PE:
                            myVal = exeCur.getNbPE();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_QE:
                            myVal = exeCur.getNbQE();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_QU:
                            myVal = exeCur.getNbQU();
                            break;
                        case TestCaseExecution.CONTROLSTATUS_WE:
                            myVal = exeCur.getNbWE();
                            break;
                    }

                    if (curveTagObjValMap.containsKey(curveKeyStatus)) {
                        tempList = curveTagObjValMap.get(curveKeyStatus);
                        tempList.add(myVal);
                    } else {
                        tempList = new ArrayList<>();
                        tempList.add(myVal);
                    }
                    curveTagObjValMap.put(curveKeyStatus, tempList);

                    if (curveTagObjValTotMap.containsKey(curveKeyStatus)) {
                        curveTagObjValTotMap.put(curveKeyStatus, curveTagObjValTotMap.get(curveKeyStatus) + myVal);
                    } else {
                        curveTagObjValTotMap.put(curveKeyStatus, myVal);
                    }

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

        JSONObject objectdist = getAllDistinct(countryMap, environmentMap, robotDecliMap, ciResultMap, countriesDefined, environmentsDefined, robotDeclisDefined, ciResultsDefined);
        object.put("distinct", objectdist);

        /**
         * Bar Charts per control status to JSON.
         */
        curvesArray = new JSONArray();
        for (String entry : curveBarMap) {
            curvesArray.put(entry);
        }
        object.put("curvesTag", curvesArray);

        curvesArray = new JSONArray();

        JSONObject objStat = new JSONObject();
        JSONObject objStatKey = new JSONObject();
        JSONArray curvesArray1 = new JSONArray();

        for (TestCaseExecution.ControlStatus ctrlStat : TestCaseExecution.ControlStatus.values()) {
            objStat = new JSONObject();
            objStatKey = new JSONObject();
            objStatKey.put("key", ctrlStat.name());
            objStatKey.put("unit", "nbExe");
            objStatKey.put("totalExe", curveTagObjValTotMap.get(ctrlStat.name()));
            objStat.put("key", objStatKey);

            curvesArray1 = new JSONArray();
            if (curveTagObjValMap.containsKey(ctrlStat.name())) {
                for (Integer myInt : curveTagObjValMap.get(ctrlStat.name())) {
                    curvesArray1.put(myInt);
                }
            }
            objStat.put("points", curvesArray1);

            if (curveTagObjValTotMap.containsKey(ctrlStat.name())) {
                if (curveTagObjValTotMap.get(ctrlStat.name()) > 0) {
                    curvesArray.put(objStat);
                }
            }

        }

        objStat = new JSONObject();
        objStatKey = new JSONObject();
        objStatKey.put("key", "RETRY");
        objStatKey.put("unit", "nbExe");
        objStatKey.put("totalExe", curveTagObjValTotMap.get("RETRY"));
        objStat.put("key", objStatKey);

        curvesArray1 = new JSONArray();
        if (curveTagObjValMap.containsKey("RETRY")) {
            for (Integer myInt : curveTagObjValMap.get("RETRY")) {
                curvesArray1.put(myInt);
            }
        }
        objStat.put("points", curvesArray1);

        if (curveTagObjValTotMap.containsKey("RETRY")) {
            if (curveTagObjValTotMap.get("RETRY") > 0) {
                curvesArray.put(objStat);
            }
        }

        object.put("curvesTagStatus", curvesArray);
        item.setItem(object);
        return item;
    }

    private JSONObject getAllDistinct(
            HashMap<String, Boolean> countryMap, HashMap<String, Boolean> environmentMap, HashMap<String, Boolean> robotDecliMap, HashMap<String, Boolean> ciResultMap,
            Boolean countriesDefined, Boolean environmentsDefined, Boolean robotDeclisDefined, Boolean ciResultsDefined) throws JSONException {

        JSONObject objectdist = new JSONObject();

        JSONArray objectCdinst = new JSONArray();
        for (Map.Entry<String, Boolean> country : countryMap.entrySet()) {
            String key = country.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", formatedJSONArray(key));
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
            objectcount.put("name", formatedJSONArray(key));
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
            objectcount.put("name", formatedJSONArray(key));
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
        for (Map.Entry<String, Boolean> env : ciResultMap.entrySet()) {
            String key = env.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", formatedJSONArray(key));
            objectcount.put("hasData", ciResultMap.containsKey(key));
            if (ciResultsDefined) {
                objectcount.put("isRequested", ciResultMap.get(key));
            } else {
                objectcount.put("isRequested", true);
            }
            objectdinst.put(objectcount);
        }
        objectdist.put("ciResults", objectdinst);

        return objectdist;
    }

    private String formatedJSONArray(String in) {
        if ((in == null) || (in.equals("[]"))) {
            return "";
        } else {
            return in.replace("\"]", "").replace("[\"", "").replace("\",\"", "|");
        }
    }

    private String getKeyCurve(TestCaseExecutionHttpStat stat, String party, String type, String unit) {
        return type + "/" + party + "/" + unit + "/" + stat.getTest() + "/" + stat.getTestcase() + "/" + stat.getCountry() + "/" + stat.getEnvironment() + "/" + stat.getRobotDecli() + "/" + stat.getSystem() + "/" + stat.getApplication();
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
