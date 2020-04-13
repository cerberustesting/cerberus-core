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
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.crud.entity.TestCaseExecutionHttpStat;
import org.cerberus.crud.factory.IFactoryTestCase;
import org.cerberus.crud.service.IApplicationService;
import org.cerberus.crud.service.ITestCaseExecutionHttpStatService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.har.entity.HarStat;
import org.cerberus.service.har.entity.HarStat.Parties;
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
    
    private static final Logger LOG = LogManager.getLogger(ReadExecutionStat.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";

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
        String test = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("test"), null, "UTF8");
        String testCase = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("testcase"), null, "UTF8");
        ltc.add(factoryTestCase.create(test, testCase));
        
        List<String> parties = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("parties"), new ArrayList<>(), "UTF8");
        
        List<String> types = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("types"), new ArrayList<>(), "UTF8");
        
        List<String> units = ParameterParserUtil.parseListParamAndDecode(request.getParameterValues("units"), new ArrayList<>(), "UTF8");

        // Init Answer with potencial error from Parsing parameter.
        AnswerItem answer = new AnswerItem<>(msg);

//        Date toD = new Date();
        try {
            JSONObject jsonResponse = new JSONObject();
            answer = findStatList(appContext, request, system, ltc, fromD, toD, parties, types, units);
            jsonResponse = (JSONObject) answer.getItem();
            
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

    private AnswerItem<JSONObject> findStatList(ApplicationContext appContext, HttpServletRequest request, List<String> system, List<TestCase> ltc, Date from, Date to, List<String> parties, List<String> types, List<String> units) throws JSONException {
        
        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        testCaseExecutionHttpStatService = appContext.getBean(ITestCaseExecutionHttpStatService.class);
        applicationService = appContext.getBean(IApplicationService.class);
        testCaseService = appContext.getBean(ITestCaseService.class);
        factoryTestCase = appContext.getBean(IFactoryTestCase.class);
        
        HashMap<String, JSONArray> curveMap = new HashMap<>();
        HashMap<String, JSONObject> curveObjMap = new HashMap<>();
        
        HashMap<String, Boolean> countryMap = new HashMap<>();
        HashMap<String, Boolean> systemMap = new HashMap<>();
        HashMap<String, TestCase> testCaseMap = new HashMap<>();
        HashMap<String, Boolean> applicationMap = new HashMap<>();
        HashMap<String, Boolean> environmentMap = new HashMap<>();
        HashMap<String, Boolean> robotDecliMap = new HashMap<>();
        HashMap<String, Boolean> partyMap = new HashMap<>();
        HashMap<String, Boolean> typeMap = new HashMap<>();
        HashMap<String, Boolean> unitMap = new HashMap<>();
        
        String graphKey = "";
        String curveKey = "";
        JSONArray curArray = new JSONArray();
        JSONObject curveObj = new JSONObject();
        JSONObject point = new JSONObject();
        
        AnswerList<TestCaseExecutionHttpStat> resp = testCaseExecutionHttpStatService.readByCriteria("OK", ltc, from, to, system, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (TestCaseExecutionHttpStat statCur : (List<TestCaseExecutionHttpStat>) resp.getDataList()) {
                
                for (String party : parties) {
                    for (String type : types) {
                        for (String unit : units) {
                            curveKey = getKeyCurve(statCur, party, type, unit);
                            int x = getValue(statCur, party, type, unit);
                            LOG.debug("return : " + x);
                            if (x != -1) {
                                countryMap.put(statCur.getCountry(), true);
                                systemMap.put(statCur.getSystem(), true);
                                testCaseMap.put(statCur.getTest() + "/\\" + statCur.getTestCase(), factoryTestCase.create(statCur.getTest(), statCur.getTestCase()));
                                applicationMap.put(statCur.getApplication(), true);
                                environmentMap.put(statCur.getEnvironment(), true);
                                robotDecliMap.put(statCur.getRobotDecli(), true);
                                partyMap.put(party, true);
                                typeMap.put(type, true);
                                unitMap.put(unit, true);
                                
                                point = new JSONObject();
                                Date d = new Date(statCur.getStart().getTime());
                                TimeZone tz = TimeZone.getTimeZone("UTC");
                                DateFormat df = new SimpleDateFormat(DATE_FORMAT);
                                df.setTimeZone(tz);
                                point.put("x", df.format(d));
                                
                                point.put("y", x);
                                point.put("exe", statCur.getId());
                                
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
                                    curveObj.put("environement", statCur.getEnvironment());
                                    curveObj.put("robotdecli", statCur.getRobotDecli());
                                    curveObj.put("system", statCur.getSystem());
                                    curveObj.put("application", statCur.getApplication());
                                    curveObj.put("unit", unit);
                                    curveObj.put("party", party);
                                    curveObj.put("type", type);
                                    
                                    curveObjMap.put(curveKey, curveObj);
                                }
                                curArray.put(point);
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
        object.put("curves", curvesArray);
        
        JSONObject objectdist = getAllDistinct(countryMap, systemMap, testCaseMap, applicationMap, environmentMap, robotDecliMap, partyMap, typeMap, unitMap);
        object.put("distinct", objectdist);
        
        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());
        
        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }
    
    private JSONObject getAllDistinct(HashMap<String, Boolean> countryMap,
            HashMap<String, Boolean> systemMap,
            HashMap<String, TestCase> testCaseMap,
            HashMap<String, Boolean> applicationMap,
            HashMap<String, Boolean> environmentMap,
            HashMap<String, Boolean> robotDecliMap,
            HashMap<String, Boolean> partyMap,
            HashMap<String, Boolean> typeMap,
            HashMap<String, Boolean> unitMap) throws JSONException {
        
        JSONObject objectdist = new JSONObject();
        
        JSONArray objectCdinst = new JSONArray();
        for (Map.Entry<String, Boolean> country : countryMap.entrySet()) {
            String key = country.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectCdinst.put(objectcount);
        }
        objectdist.put("countries", objectCdinst);
        
        JSONArray objectSdinst = new JSONArray();
        for (Map.Entry<String, Boolean> sys : systemMap.entrySet()) {
            String key = sys.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectSdinst.put(objectcount);
        }
        objectdist.put("systems", objectSdinst);
        
        JSONArray objectTCdinst = new JSONArray();
        for (Map.Entry<String, TestCase> env : testCaseMap.entrySet()) {
            try {
                String key = env.getKey();
                TestCase tc = env.getValue();
//                JSONObject objectcount = new JSONObject();
                TestCase a = testCaseService.convert(testCaseService.readByKey(tc.getTest(), tc.getTestCase()));
//                objectcount.put("testCase", a.toJson());
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
//                JSONObject objectcount = new JSONObject();
                Application a = applicationService.convert(applicationService.readByKey(key));
//                objectcount.put("application", convertApplicationToJSONObject(a));
                objectAdinst.put(convertApplicationToJSONObject(a));
            } catch (CerberusException ex) {
                LOG.error("Exception when getting Application.", ex);
            }
        }
        objectdist.put("applications", objectAdinst);
        
        JSONArray objectdinst = new JSONArray();
        for (Map.Entry<String, Boolean> env : environmentMap.entrySet()) {
            String key = env.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectdinst.put(objectcount);
        }
        objectdist.put("environments", objectdinst);
        
        objectdinst = new JSONArray();
        for (Map.Entry<String, Boolean> env : robotDecliMap.entrySet()) {
            String key = env.getKey();
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", key);
            objectdinst.put(objectcount);
        }
        objectdist.put("robotDeclis", objectdinst);
        
        objectdinst = new JSONArray();
        for (Units v : HarStat.Units.values()) {
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", v.name().toLowerCase());
            objectcount.put("isUsed", unitMap.containsKey(v.name().toLowerCase()));
            objectdinst.put(objectcount);
        }
        objectdist.put("units", objectdinst);
        
        objectdinst = new JSONArray();
        for (Types v : HarStat.Types.values()) {
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", v.name().toLowerCase());
            objectcount.put("isUsed", typeMap.containsKey(v.name().toLowerCase()));
            objectdinst.put(objectcount);
        }
        objectdist.put("types", objectdinst);
        
        objectdinst = new JSONArray();
        for (Parties v : HarStat.Parties.values()) {
            JSONObject objectcount = new JSONObject();
            objectcount.put("name", v.name().toLowerCase());
            objectcount.put("isUsed", partyMap.containsKey(v.name().toLowerCase()));
            objectdinst.put(objectcount);
        }
        objectdist.put("parties", objectdinst);
        
        return objectdist;
    }
    
    private JSONObject convertStatToJSONObject(TestCaseExecutionHttpStat stat) throws JSONException {
        
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(stat));
        return result;
    }
    
    private int getValue(TestCaseExecutionHttpStat stat, String party, String type, String unit) {
        LOG.debug("Start." + party + " - " + type + " - " + unit);
        switch (party) {
            case "internal":
                switch (type) {
                    case "img":
                        switch (unit) {
                            case "request":
                                break;
                            case "size":
                                break;
                            case "time":
                                break;
                            default:
                                break;
                        }
                        break;
                    case "total":
                        switch (unit) {
                            case "request":
                                return stat.getInternal_hits();
                            case "size":
                                return stat.getInternal_size();
                            case "time":
                                return stat.getInternal_time();
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
                    case "img":
                        switch (unit) {
                            case "request":
                                return stat.getImg_hits();
                            case "size":
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
                            case "size":
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
                            case "size":
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
                            case "size":
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
                            case "size":
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
                            case "size":
                                return stat.getTotal_size();
                            case "time":
                                return stat.getTotal_time();
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return -1;
    }
    
    private JSONObject convertApplicationToJSONObject(Application app) throws JSONException {
        
        Gson gson = new Gson();
        JSONObject result = new JSONObject(gson.toJson(app));
        return result;
    }
    
    private String getKeyCurve(TestCaseExecutionHttpStat stat, String party, String type, String unit) {
        return type + "/" + party + "/" + unit + "/" + stat.getTest() + "/" + stat.getTestCase() + "/" + stat.getCountry() + "/" + stat.getEnvironment() + "/" + stat.getRobotDecli() + "/" + stat.getSystem() + "/" + stat.getApplication();
    }
}
