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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseExecution.ControlStatus;
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
import org.cerberus.core.util.answer.AnswerList;
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
@WebServlet(name = "ReadExecutionTagHistory", urlPatterns = {"/ReadExecutionTagHistory"})
public class ReadExecutionTagHistory extends HttpServlet {

    private ITestCaseExecutionHttpStatService testCaseExecutionHttpStatService;
    private IFactoryTestCase factoryTestCase;
    private IApplicationService applicationService;
    private ITestCaseService testCaseService;
    private ITagService tagService;

    private static final Logger LOG = LogManager.getLogger(ReadExecutionTagHistory.class);
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
        List<String> systems = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("system"), new ArrayList<>(), "UTF8");
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

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem<JSONObject> answer = new AnswerItem<>(msg);

        try {

            JSONObject jsonResponse = new JSONObject();
            answer = findTagHistoData(appContext, request, systems, fromD, toD);
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

    private AnswerItem<JSONObject> findTagHistoData(ApplicationContext appContext, HttpServletRequest request,
            List<String> system, Date from, Date to) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        testCaseExecutionHttpStatService = appContext.getBean(ITestCaseExecutionHttpStatService.class);
        applicationService = appContext.getBean(IApplicationService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);
        tagService = appContext.getBean(ITagService.class);

        HashMap<String, JSONObject> curveStatusObjMap = new HashMap<>();
        TreeMap<String, Boolean> curveDateMap = new TreeMap<>();
        TreeMap<String, Integer> curveDateStatusMap = new TreeMap<>();
        TreeMap<String, Integer> curveStatusMap = new TreeMap<>();

        String curveKeyStatus;
        JSONArray curArray = new JSONArray();
        JSONObject curveStatObj = new JSONObject();
        JSONObject pointObj = new JSONObject();

        // get all TestCase Execution Data.
        AnswerList<Tag> resp = tagService.readByVarious(new ArrayList<>(), system, from, to);

        // Building the list of status to load adding the extra RETRY.
        List<String> statList = new ArrayList<>();
        for (ControlStatus v : TestCaseExecution.ControlStatus.values()) {
            statList.add(v.name().toUpperCase());
        }
        statList.add("RETRY");

        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            List<Tag> tcList = resp.getDataList();
            for (Tag tagCur : tcList) {

                /**
                 * Bar Charts per control status.
                 */
                for (String v : statList) {

                    curveKeyStatus = v.toUpperCase();
                    int x = getValue(tagCur, curveKeyStatus);

                    Date d = new Date(tagCur.getDateCreated().getTime());
                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat(DATE_FORMAT_DAY);
                    df.setTimeZone(tz);
                    String dday = df.format(d);

                    curveDateMap.put(dday, false);

                    String keyDateStatus = curveKeyStatus + "-" + dday;
                    if (curveDateStatusMap.containsKey(keyDateStatus)) {
                        curveDateStatusMap.put(keyDateStatus, curveDateStatusMap.get(keyDateStatus) + x);
                    } else {
                        curveDateStatusMap.put(keyDateStatus, x);
                    }

                    if (curveStatusMap.containsKey(curveKeyStatus)) {
                        curveStatusMap.put(curveKeyStatus, curveStatusMap.get(curveKeyStatus) + x);
                    } else {
                        curveStatusMap.put(curveKeyStatus, x);
                    }

                    if (!curveStatusObjMap.containsKey(curveKeyStatus)) {

                        curveStatObj = new JSONObject();
                        curveStatObj.put("key", curveKeyStatus);
                        curveStatObj.put("unit", "nbExe");

                        curveStatusObjMap.put(curveKeyStatus, curveStatObj);
                    }
                }

            }
            object.put("hasHistodata", (tcList.size() > 0));
        }

        /**
         * Bar Charts per control status to JSON.
         */
        JSONArray curvesArray = new JSONArray();
        for (Map.Entry<String, Boolean> entry : curveDateMap.entrySet()) {
            curvesArray.put(entry.getKey());
        }
        object.put("curvesDatesNb", curvesArray);

        curvesArray = new JSONArray();
        for (Map.Entry<String, JSONObject> entry : curveStatusObjMap.entrySet()) {
            String key = entry.getKey();
            if (curveStatusMap.get(key) > 0) {
                JSONObject val = entry.getValue();
                val.put("nbExe", curveStatusMap.get(key));

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
        }
        object.put("curvesNb", curvesArray);

        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    private int getValue(Tag tagCur, String status) {
        switch (status) {
            case "OK":
                return tagCur.getNbOK();
            case "KO":
                return tagCur.getNbKO();
            case "FA":
                return tagCur.getNbFA();
            case "NA":
                return tagCur.getNbNA();
            case "NE":
                return tagCur.getNbNE();
            case "WE":
                return tagCur.getNbWE();
            case "PE":
                return tagCur.getNbPE();
            case "QU":
                return tagCur.getNbQU();
            case "QE":
                return tagCur.getNbQE();
            case "CA":
                return tagCur.getNbCA();
            case "RETRY":
                return tagCur.getNbExe() - tagCur.getNbExeUsefull();
        }
        return 0;
    }

    private JSONObject convertApplicationToJSONObject(Application app) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(app));
        return result;
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
