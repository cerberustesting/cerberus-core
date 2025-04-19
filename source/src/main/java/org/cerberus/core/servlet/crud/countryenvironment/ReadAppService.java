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
package org.cerberus.core.servlet.crud.countryenvironment;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.dto.TestCaseListDTO;
import org.cerberus.core.dto.TestListDTO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.util.servlet.ServletUtil;
import org.cerberus.core.exception.CerberusException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cerberus.core.util.StringUtil;

/**
 * @author bcivel
 */
@WebServlet(name = "ReadAppService", urlPatterns = {"/ReadAppService"})
public class ReadAppService extends HttpServlet {

    public static final Pattern ALL_VARIABLE_PATTERN = Pattern.compile("%datalib\\..*%|%property\\..*%|%system\\..*%");

    private static final Logger LOG = LogManager.getLogger(ReadAppService.class);

    private IAppServiceService appServiceService;
    private IApplicationService applicationService;
    private ICountryEnvironmentParametersService cepService;

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
        //Get AppServices

        String echo = request.getParameter("sEcho");
        String columnName = ParameterParserUtil.parseStringParam(request.getParameter("columnName"), "");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Default message to unexpected error.
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        /**
         * Parsing and securing all required soapLibrarys.
         */
        // Nothing to do here as no soapLibrary to check.
        //
        // Global boolean on the servlet that define if the user has permition to edit and delete object.
        boolean userHasPermissions = request.isUserInRole("TestAdmin");

        // Init Answer with potencial error from Parsing soapLibrary.
        AnswerItem<JSONObject> answer = new AnswerItem<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK));
        boolean testcase = ParameterParserUtil.parseBooleanParam(request.getParameter("testcase"), false);
        String service = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("service"), null, charset);

        try {

            String system;
            JSONObject jsonResponse = new JSONObject();

            if (service == null && StringUtil.isEmptyOrNull(columnName)) {
                answer = findAppServiceList(appContext, userHasPermissions, request);
                jsonResponse = answer.getItem();
            } else if (!StringUtil.isEmptyOrNull(columnName)) {
                answer = findDistinctValuesOfColumn(appContext, request, columnName);
                jsonResponse = answer.getItem();
            } else if (service != null && request.getParameter("limit") != null) {
                answer = findAppServiceByLikeName(service, appContext, Integer.parseInt(request.getParameter("limit")));
                jsonResponse = answer.getItem();
            } else if (service != null && testcase) {
                answer = getTestCasesUsingService(service, appContext);
                jsonResponse = answer.getItem();
            } else {
                answer = findAppServiceBySystemByKey(service, appContext, userHasPermissions);
                jsonResponse = answer.getItem();
            }

            jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", answer.getResultMessage().getDescription());
            jsonResponse.put("sEcho", echo);

            response.getWriter().print(jsonResponse.toString());

        } catch (JSONException e) {
            LOG.warn(e, e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
    }

    private AnswerItem<JSONObject> findAppServiceList(ApplicationContext appContext, boolean userHasPermissions, HttpServletRequest request) throws JSONException {

        AnswerItem<JSONObject> item = new AnswerItem<>();
        JSONObject object = new JSONObject();
        appServiceService = appContext.getBean(IAppServiceService.class);

        int startPosition = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0"));
        int length = Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "0"));
        /*int sEcho  = Integer.valueOf(request.getParameter("sEcho"));*/

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        int columnToSortParameter = Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0"));
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "service,type,method,description");
        String columnToSort[] = sColumns.split(",");
        String columnName = columnToSort[columnToSortParameter];
        String sort = ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc");
        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));
        List<String> systems = ParameterParserUtil.parseListParamAndDeleteEmptyValue(request.getParameterValues("system"), Arrays.asList("DEFAULT"), "UTF-8");

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }

        AnswerList<AppService> resp = appServiceService.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch, systems);

        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (AppService param : resp.getDataList()) {
                jsonArray.put(convertAppServiceToJSONObject(param));
            }
        }

        object.put("hasPermissions", userHasPermissions);
        object.put("contentTable", jsonArray);
        object.put("iTotalRecords", resp.getTotalRows());
        object.put("iTotalDisplayRecords", resp.getTotalRows());

        item.setItem(object);
        item.setResultMessage(resp.getResultMessage());
        return item;
    }

    private AnswerItem<JSONObject> findAppServiceBySystemByKey(String key, ApplicationContext appContext, boolean userHasPermissions) throws JSONException {
        AnswerItem<JSONObject> answerItem = new AnswerItem<>();

        JSONObject response = new JSONObject();
        appServiceService = appContext.getBean(IAppServiceService.class);
        applicationService = appContext.getBean(IApplicationService.class);
        cepService = appContext.getBean(ICountryEnvironmentParametersService.class);

        AnswerItem<AppService> resp = appServiceService.readByKeyWithDependency(key);
        AppService p = null;
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            p = resp.getItem();
        }
        JSONObject item = convertAppServiceToJSONObject(p);
        response.put("contentTable", item);
        if (p != null) {
            item.put("hasPermissions", userHasPermissions);
            if (StringUtil.isNotEmptyOrNull(p.getApplication())) {
                try {
                    String system = "";

                    Application app = applicationService.convert(applicationService.readByKey(p.getApplication()));
                    system = app.getSystem();
                    List<CountryEnvironmentParameters> cepValue = cepService.convert(cepService.readByVarious(system, null, null, p.getApplication()));
                    Map<String, String> distinctEnv = new HashMap<>();
                    Map<String, String> distinctCountry = new HashMap<>();
                    for (CountryEnvironmentParameters countryEnvironmentParameters : cepValue) {
                        distinctCountry.put(countryEnvironmentParameters.getCountry(), "");
                        distinctEnv.put(countryEnvironmentParameters.getEnvironment(), "");
                    }
                    JSONObject extraInfo = new JSONObject();
                    extraInfo.put("system", system);
                    JSONArray countries = new JSONArray();
                    JSONArray environments = new JSONArray();

                    for (Map.Entry<String, String> entry : distinctCountry.entrySet()) {
                        String mycountry = entry.getKey();
                        countries.put(mycountry);
                    }
                    for (Map.Entry<String, String> entry : distinctEnv.entrySet()) {
                        String myenv = entry.getKey();
                        environments.put(myenv);
                    }
                    extraInfo.put("countries", countries);
                    extraInfo.put("environments", environments);
                    response.put("extraInformation", extraInfo);

                } catch (CerberusException e) {
                    LOG.error("Detailed information could not be retrieved for application '" + p.getApplication() + "'", e);
                } catch (Exception e) {
                    LOG.error("Detailed information could not be retrieved for application '" + p.getApplication() + "'", e);
                }

            }
        }
        answerItem.setItem(response);
        answerItem.setResultMessage(resp.getResultMessage());

        return answerItem;
    }

    private AnswerItem<JSONObject> findAppServiceByLikeName(String key, ApplicationContext appContext, int limit) throws JSONException {
        AnswerItem<JSONObject> answerItem = new AnswerItem<>();
        JSONObject response = new JSONObject();
        appServiceService = appContext.getBean(IAppServiceService.class);
        AnswerList<AppService> resp = appServiceService.readByLikeName(key, limit);
        AppService p = null;
        JSONArray jsonArray = new JSONArray();
        if (resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {//the service was able to perform the query, then we should get all values
            for (AppService appService : resp.getDataList()) {
                jsonArray.put(convertAppServiceToJSONObject(appService));
            }
        }

        response.put("contentTable", jsonArray);
        response.put("iTotalRecords", resp.getTotalRows());
        response.put("iTotalDisplayRecords", resp.getTotalRows());

        answerItem.setItem(response);
        answerItem.setResultMessage(resp.getResultMessage());
        return answerItem;
    }

    /**
     * Auxiliary method that extracts the list of test cases that are currently
     * using one service.
     *
     * @param appContext - context object used to get the required beans
     * @param service - identifier of the service
     * @return an answer item containing the information about the test cases
     * that use the entry
     * @throws JSONException
     */
    private AnswerItem<JSONObject> getTestCasesUsingService(String service, ApplicationContext appContext) throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray objectArray = new JSONArray();
        AnswerItem<JSONObject> ansItem = new AnswerItem<>();
        ITestCaseService tcService = appContext.getBean(ITestCaseService.class);

        AnswerList<TestListDTO> ansList = tcService.findTestCasesThatUseService(service);

        //if the response is success then we can iterate and search for the data
        if (ansList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            List<TestListDTO> listDTO = ansList.getDataList();
            for (TestListDTO l : listDTO) {
                JSONArray jsonArray = new JSONArray();
                JSONArray arrTestCase = new JSONArray();
                for (TestCaseListDTO testCase : l.getTestCaseList()) {
                    JSONObject jsonTestCase = new JSONObject();

                    jsonTestCase.put("TestCaseNumber", testCase.getTestCaseNumber());
                    jsonTestCase.put("TestCaseDescription", testCase.getTestCaseDescription());
                    jsonTestCase.put("Creator", testCase.getCreator());
                    jsonTestCase.put("Active", testCase.isIsActive());
                    jsonTestCase.put("Status", testCase.getStatus());
                    jsonTestCase.put("Group", testCase.getGroup());
                    jsonTestCase.put("Application", testCase.getApplication());
                    arrTestCase.put(jsonTestCase);
                }
                //test details
                jsonArray.put(l.getTest());
                jsonArray.put(l.getDescription());
                jsonArray.put(l.getTestCaseList().size());
                jsonArray.put(arrTestCase);
                //test case details
                objectArray.put(jsonArray);
            }
        }

        object.put("TestCasesList", objectArray);
        ansItem.setItem(object);
        ansItem.setResultMessage(ansList.getResultMessage());
        return ansItem;
    }

    private AnswerItem<JSONObject> findDistinctValuesOfColumn(ApplicationContext appContext, HttpServletRequest request, String columnName) throws JSONException {
        AnswerItem<JSONObject> answer = new AnswerItem<>();
        JSONObject object = new JSONObject();

        appServiceService = appContext.getBean(IAppServiceService.class);

        String searchParameter = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");
        String sColumns = ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), "para,valC,valS,descr");
        String columnToSort[] = sColumns.split(",");

        List<String> individualLike = new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(",")));

        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }
            }
        }

        AnswerList applicationList = appServiceService.readDistinctValuesByCriteria(searchParameter, individualSearch, columnName);

        object.put("distinctValues", applicationList.getDataList());

        answer.setItem(object);
        answer.setResultMessage(applicationList.getResultMessage());
        return answer;
    }

    private JSONObject convertAppServiceToJSONObject(AppService appservice) throws JSONException {

        Gson gson = new Gson();
        JSONObject result = new JSONObject();

        if (appservice != null) {
            result = new JSONObject(gson.toJson(appservice));
            result.remove("map");
            result.put("simulationParameters", appservice.getSimulationParameters());
            String pass = StringUtil.getPasswordFromAnyUrl(result.getString("servicePath"));
            if (pass != null) {
                result.put("servicePath", result.getString("servicePath").replace(pass, StringUtil.SECRET_STRING));
            }
            if (StringUtil.isNotEmptyOrNull(appservice.getAuthPassword())) {
                Matcher datalibMatcher = ALL_VARIABLE_PATTERN.matcher(appservice.getAuthPassword());
                if (datalibMatcher.find()) {
                    result.put("authPassword", appservice.getAuthPassword());
                } else {
                    result.put("authPassword", StringUtil.SECRET_STRING);
                }
            }
        }
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
