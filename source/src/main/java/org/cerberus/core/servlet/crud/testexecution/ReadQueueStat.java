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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.QueueStat;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.IQueueStatService;
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
@WebServlet(name = "ReadQueueStat", urlPatterns = {"/ReadQueueStat"})
public class ReadQueueStat extends HttpServlet {

    private ITestCaseExecutionHttpStatService testCaseExecutionHttpStatService;
    private IFactoryTestCase factoryTestCase;
    private ITestCaseService testCaseService;
    private IApplicationService applicationService;
    private IQueueStatService queueStatService;

    private static final Logger LOG = LogManager.getLogger(ReadQueueStat.class);
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

            queueStatService = appContext.getBean(IQueueStatService.class);
            AnswerList<QueueStat> answerStat = new AnswerList<>(msg);
            answerStat = queueStatService.readByCriteria(fromD, toD);
            List<QueueStat> queueStatL = queueStatService.convert(answerStat);

            JSONObject jsonResponse = new JSONObject();

            jsonResponse.put("messageType", answerStat.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answerStat.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            JSONObject jsonResponse1 = new JSONObject();
            answer = findQueueStatList(appContext, request, queueStatL);
            jsonResponse1 = answer.getItem();

            jsonResponse.put("datasetQueueStat", jsonResponse1.getJSONArray("datasetQueueStat"));

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private AnswerItem<JSONObject> findQueueStatList(ApplicationContext appContext, HttpServletRequest request,
            List<QueueStat> exeList) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        testCaseExecutionHttpStatService = appContext.getBean(ITestCaseExecutionHttpStatService.class);
        applicationService = appContext.getBean(IApplicationService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);

        HashMap<String, JSONArray> curveMap = new HashMap<>();
        HashMap<String, JSONObject> curveObjMap = new HashMap<>();

        JSONArray cur1Array = new JSONArray();
        JSONArray cur2Array = new JSONArray();
        JSONArray cur3Array = new JSONArray();
        JSONObject curveObj = new JSONObject();
        JSONObject point1Obj = new JSONObject();
        JSONObject point2Obj = new JSONObject();
        JSONObject point3Obj = new JSONObject();

        HashMap<String, JSONObject> curveStatusObjMap = new HashMap<>();
        HashMap<String, Boolean> curveDateMap = new HashMap<>();
        HashMap<String, Integer> curveDateStatusMap = new HashMap<>();

        String curveKeyStatus = "";
        JSONObject curveStatObj = new JSONObject();

        for (QueueStat exeCur : exeList) {

            /**
             * Curves of testcase response time.
             */
            long y1 = 0;
            long y2 = 0;
            long y3 = 0;

            Date d = new Date(exeCur.getDateCreated().getTime());
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setTimeZone(tz);

            y1 = exeCur.getCurrentlyRunning();
            point1Obj = new JSONObject();
            point1Obj.put("x", df.format(d));
            point1Obj.put("y", y1);
            cur1Array.put(point1Obj);

            y2 = exeCur.getGlobalConstrain();
            point2Obj = new JSONObject();
            point2Obj.put("x", df.format(d));
            point2Obj.put("y", y2);
            cur2Array.put(point2Obj);

            y3 = exeCur.getQueueSize();
            point3Obj = new JSONObject();
            point3Obj.put("x", df.format(d));
            point3Obj.put("y", y3);
            cur3Array.put(point3Obj);

        }

        /**
         * Feed Curves of testcase response time to JSON.
         */
        JSONArray curvesArray = new JSONArray();

        JSONObject val = new JSONObject();
        val.put("key", "CurrentlyRunning");
        JSONObject localcur = new JSONObject();
        localcur.put("key", val);
        localcur.put("points", cur1Array);
        curvesArray.put(localcur);

        val = new JSONObject();
        val.put("key", "GlobalConstrain");
        localcur = new JSONObject();
        localcur.put("key", val);
        localcur.put("points", cur2Array);
        curvesArray.put(localcur);

        val = new JSONObject();
        val.put("key", "QueueSize");
        localcur = new JSONObject();
        localcur.put("key", val);
        localcur.put("points", cur3Array);
        curvesArray.put(localcur);

        object.put("datasetQueueStat", curvesArray);

        item.setItem(object);
        return item;
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
