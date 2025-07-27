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
package org.cerberus.core.servlet.information;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.cerberus.core.config.cerberus.Property;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.database.dao.ICerberusInformationDAO;
import org.cerberus.core.engine.entity.ExecutionUUID;
import org.cerberus.core.session.SessionCounter;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IMyVersionService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagSystemService;
import org.cerberus.core.database.IDatabaseVersioningService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.engine.scheduler.SchedulerInit;
import org.cerberus.core.service.xray.IXRayService;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.version.Infos;
import org.cerberus.core.websocket.ExecutionMonitor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ReadCerberusDetailInformation", urlPatterns = {"/ReadCerberusDetailInformation"})
public class ReadCerberusDetailInformation extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadCerberusDetailInformation.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S";

    private ICerberusInformationDAO cerberusDatabaseInformation;
    private IDatabaseVersioningService databaseVersionService;
    private IMyVersionService myVersionService;
    private IParameterService parameterService;
    private ITagSystemService tagSystemService;
    private IExecutionThreadPoolService executionThreadPoolService;
    private IXRayService xrayService;
    private ExecutionMonitor monitorExe;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject jsonResponse = new JSONObject();
        
        try {

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ExecutionUUID euuid = appContext.getBean(ExecutionUUID.class);
            SessionCounter sc = appContext.getBean(SessionCounter.class);
            SchedulerInit scInit = appContext.getBean(SchedulerInit.class);

            Infos infos = new Infos();

            // ######### runningExecutionsList #########
            JSONArray executionArray = new JSONArray();
            for (Object ex : euuid.getExecutionUUIDList().values()) {
                TestCaseExecution execution = (TestCaseExecution) ex;
                JSONObject object = new JSONObject();
                object.put("id", execution.getId());
                object.put("test", execution.getTest());
                object.put("testcase", execution.getTestCase());
                object.put("system", execution.getApplicationObj().getSystem());
                object.put("application", execution.getApplication());
                object.put("environment", execution.getEnvironmentData());
                object.put("country", execution.getCountry());
                object.put("robotIP", execution.getSeleniumIP());
                object.put("tag", execution.getTag());
                object.put("start", new Timestamp(execution.getStart()));
                executionArray.put(object);
            }
            jsonResponse.put("runningExecutionsList", executionArray);

            // ######### sessions #########
            JSONObject session = new JSONObject();
            session.put("simultaneous_session", sc.getTotalActiveSession());
            session.put("active_users", sc.getActiveUsers());
            jsonResponse.put("sessions", session);

            // ######### scheduler #########
            JSONObject schedulerObject = new JSONObject();
            if (scInit != null) {
                schedulerObject.put("schedulerInstanceVersion", scInit.getInstanceSchedulerVersion());
                schedulerObject.put("schedulerReloadIsRunning", scInit.isIsRunning());
                // We get here the list of triggers of Quartz scheduler.
                List<JSONObject> triggerList = new ArrayList<>();
                for (Trigger triggerSet : scInit.getMyTriggersSet()) {
                    JSONObject objectTrig = new JSONObject();
                    objectTrig.put("triggerId", triggerSet.getJobDataMap().getLong("schedulerId"));
                    objectTrig.put("triggerName", triggerSet.getJobDataMap().getString("name"));
                    objectTrig.put("triggerType", triggerSet.getJobDataMap().getString("type"));
                    objectTrig.put("triggerUserCreated", triggerSet.getJobDataMap().getString("user"));
                    objectTrig.put("triggerNextFiretime", new SimpleDateFormat(DATE_FORMAT).format(triggerSet.getFireTimeAfter(new Date())));
                    objectTrig.put("triggerCronDefinition", triggerSet.getJobDataMap().getString("cronDefinition"));
                    triggerList.add(objectTrig);
                }
                Collections.sort(triggerList, new SortTriggers());
                JSONArray object1 = new JSONArray(triggerList);
                schedulerObject.put("schedulerTriggers", object1);
                Date now = new Date();
                schedulerObject.put("serverDate", new SimpleDateFormat(DATE_FORMAT).format(now));
                schedulerObject.put("serverTimeZone", TimeZone.getDefault().getDisplayName());
            }
            jsonResponse.put("scheduler", schedulerObject);

            // ######### queueStats #########
            JSONObject queueObject = new JSONObject();
            queueObject.put("globalLimit", euuid.getGlobalLimit());
            queueObject.put("running", euuid.getRunning());
            queueObject.put("queueSize", euuid.getQueueSize());
            jsonResponse.put("queueStats", queueObject);

            // ######### database #########
            cerberusDatabaseInformation = appContext.getBean(ICerberusInformationDAO.class);
            AnswerItem<HashMap<String, String>> ans = cerberusDatabaseInformation.getDatabaseInformation();
            HashMap<String, String> cerberusInformation = ans.getItem();

            JSONObject databaseinfo = new JSONObject();
            databaseinfo.put("databaseProductName", cerberusInformation.get("DatabaseProductName"));
            databaseinfo.put("databaseProductVersion", cerberusInformation.get("DatabaseProductVersion"));
            databaseinfo.put("databaseMajorVersion", cerberusInformation.get("DatabaseMajorVersion"));
            databaseinfo.put("databaseMinorVersion", cerberusInformation.get("DatabaseMinorVersion"));

            databaseinfo.put("driverName", cerberusInformation.get("DriverName"));
            databaseinfo.put("driverVersion", cerberusInformation.get("DriverVersion"));
            databaseinfo.put("driverMajorVersion", cerberusInformation.get("DriverMajorVersion"));
            databaseinfo.put("driverMinorVersion", cerberusInformation.get("DriverMinorVersion"));

            databaseinfo.put("jDBCMajorVersion", cerberusInformation.get("JDBCMajorVersion"));
            databaseinfo.put("jDBCMinorVersion", cerberusInformation.get("JDBCMinorVersion"));

            databaseVersionService = appContext.getBean(IDatabaseVersioningService.class);
            databaseinfo.put("databaseCerberusTargetVersion", databaseVersionService.getSqlVersion());

            myVersionService = appContext.getBean(IMyVersionService.class);
            if (myVersionService.findMyVersionByKey("database") != null) {
                databaseinfo.put("databaseCerberusCurrentVersion", myVersionService.findMyVersionByKey("database").getValue());
            } else {
                databaseinfo.put("databaseCerberusCurrentVersion", "0");
            }
            jsonResponse.put("database", databaseinfo);

            // ######### cerberus #########
            JSONObject crbinfo = new JSONObject();
            crbinfo.put("projectName", infos.getProjectName());
            crbinfo.put("projectVersion", infos.getProjectVersion());
            crbinfo.put("projectBuild", infos.getProjectBuildId());
            crbinfo.put("environment", System.getProperty(Property.ENVIRONMENT));
            jsonResponse.put("cerberus", crbinfo);

            // ######### authentification #########
            JSONObject authinfo = new JSONObject();
            authinfo.put("authentification", System.getProperty(Property.AUTHENTIFICATION));
            authinfo.put("isKeycloak", Property.isKeycloak());
            authinfo.put("keycloakRealm", System.getProperty(Property.KEYCLOAKREALM));
            authinfo.put("keycloakClient", System.getProperty(Property.KEYCLOAKCLIENT));
            authinfo.put("keycloakUrl", System.getProperty(Property.KEYCLOAKURL));
            jsonResponse.put("authentification", authinfo);

            // ######### saasinfo #########
            JSONObject saasinfo = new JSONObject();
            parameterService = appContext.getBean(IParameterService.class);
            saasinfo.put("saaS", System.getProperty(Property.SAAS));
            saasinfo.put("isSaaS", Property.isSaaS());
            saasinfo.put("saasInstance", System.getProperty(Property.SAASINSTANCE));
            saasinfo.put("saasParallelrun", parameterService.getParameterIntegerByKey("cerberus_queueexecution_global_threadpoolsize", "", 12));
            jsonResponse.put("saasinfo", saasinfo);

            // ######### java #########
            JSONObject javainfo = new JSONObject();
            javainfo.put("javaVersion", System.getProperty("java.version"));
            Runtime instance = Runtime.getRuntime();
            int mb = 1024 * 1024;
            javainfo.put("javaFreeMemory", instance.freeMemory() / mb);
            javainfo.put("javaTotalMemory", instance.totalMemory() / mb);
            javainfo.put("javaUsedMemory", (instance.totalMemory() - instance.freeMemory()) / mb);
            javainfo.put("javaMaxMemory", instance.maxMemory() / mb);

            String str1 = getServletContext().getServerInfo();
            javainfo.put("applicationServerInfo", str1);
            jsonResponse.put("java", javainfo);

            // ######### cache #########
            JSONObject objCache = new JSONObject();
            HashMap<String, Parameter> cacheParam = parameterService.getCacheEntry();
            JSONArray cacheValuesArray = new JSONArray();
            for (Map.Entry<String, Parameter> entry : cacheParam.entrySet()) {
                String key = entry.getKey();
                Parameter value = entry.getValue();
                JSONObject objParam = new JSONObject();
                objParam.put("key", key);
                if (value.getCacheEntryCreation() != null) {
                    objParam.put("created", value.getCacheEntryCreation().toString());
                    Duration d = Duration.between(value.getCacheEntryCreation(), LocalDateTime.now());
                    objParam.put("durationFromCreatedInS", d.getSeconds());
                }
                cacheValuesArray.put(objParam);
            }
            objCache.put("cacheParameterEntries", cacheValuesArray);
            objCache.put("cacheParameterDurationInS", Parameter.CACHE_DURATION);
            // Cache Tag System data and status
            cacheValuesArray = new JSONArray();
            tagSystemService = appContext.getBean(ITagSystemService.class);
            cacheValuesArray.put(tagSystemService.getTagSystemCache());
            objCache.put("cacheTagSystemEntries", cacheValuesArray);
            // Cache XRay data and status
            xrayService = appContext.getBean(IXRayService.class);
            objCache.put("cacheXRayEntries", xrayService.getAllCacheEntries());
            jsonResponse.put("cache", objCache);

            // ######### monitorExecutions #########
            monitorExe = appContext.getBean(ExecutionMonitor.class);
            JSONObject monitor = new JSONObject();
            monitor.put("executions", monitorExe.getExecutionHashMap());
            monitor.put("tests", monitorExe.getTestcaseMap());
            monitor.put("environments", monitorExe.getCountryEnvRobotMap());
            jsonResponse.put("monitorExecutions", monitor);
            
            // ######### creditLimit #########
            JSONObject objCreditLimit = new JSONObject();
            objCreditLimit.put("numberOfExecution", sc.getCreditLimitNbExe());
            objCreditLimit.put("durationOfExecutionInSecond", sc.getCreditLimitSecondExe());
            jsonResponse.put("creditLimit", objCreditLimit);

            // ######### executionThreadPoolInstanceActive #########
            executionThreadPoolService = appContext.getBean(IExecutionThreadPoolService.class);
            jsonResponse.put("executionThreadPoolInstanceActive", executionThreadPoolService.isInstanceActive());

        } catch (JSONException ex) {
            LOG.warn(ex);
        } catch (Exception ex) {
            LOG.error("Exception in ReadCerberusDetailInformation Servlet", ex);
        }

        response.setContentType("application/json");
        response.getWriter().print(jsonResponse.toString());
    }

    class SortTriggers implements Comparator<JSONObject> {

        // Used for sorting Triggers 
        @Override
        public int compare(JSONObject a, JSONObject b) {

            if (a != null && b != null) {
                String typeA;
                String typeB;
                try {
                    typeA = a.getString("triggerType");
                    typeB = b.getString("triggerType");
                    if (typeA.equals(typeB)) {
                        String nameA;
                        String nameB;
                        try {
                            nameA = a.getString("triggerName");
                            nameB = b.getString("triggerName");
                            if (nameA.equals(nameB)) {
                                Date dateA;
                                Date dateB;
                                try {
                                    dateA = (Date) a.get("triggerNextFiretime");
                                    dateB = (Date) b.get("triggerNextFiretime");
                                    if (dateA.equals(dateB)) {

                                    } else {
                                        return (dateA.compareTo(dateB));
                                    }
                                } catch (JSONException ex) {
                                    LOG.error("Exception on JSON Parse.", ex);
                                } catch (ClassCastException ex) {
                                    return 1;
                                }

                            } else {
                                return nameA.compareToIgnoreCase(nameB);
                            }
                        } catch (JSONException ex) {
                            LOG.error("Exception on JSON Parse.", ex);
                        }

                    } else {
                        return typeA.compareToIgnoreCase(typeB);
                    }
                } catch (JSONException ex) {
                    LOG.error("Exception on JSON Parse.", ex);
                }

            } else {
                return 1;
            }

            return 1;
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
        processRequest(request, response);
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
        processRequest(request, response);
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
