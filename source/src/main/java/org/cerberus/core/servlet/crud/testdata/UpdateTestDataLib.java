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
package org.cerberus.core.servlet.crud.testdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.TestDataLib;
import org.cerberus.core.crud.entity.TestDataLibData;
import org.cerberus.core.crud.factory.IFactoryTestDataLibData;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestDataLibDataService;
import org.cerberus.core.crud.service.ITestDataLibService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Handles the UPDATE operation for test data lib entries.
 *
 * @author FNogueira
 */
@WebServlet(name = "UpdateTestDataLib", urlPatterns = {"/UpdateTestDataLib"})
public class UpdateTestDataLib extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateTestDataLib.class);

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
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        IFactoryTestDataLibData tdldFactory = appContext.getBean(IFactoryTestDataLibData.class);

        ITestDataLibDataService tdldService = appContext.getBean(ITestDataLibDataService.class);
        IParameterService parameterService = appContext.getBean(IParameterService.class);

        response.setContentType("application/json");

        Map<String, String> fileData = new HashMap<>();
        FileItem file = null;

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List<FileItem> fields = upload.parseRequest(request);
            Iterator<FileItem> it = fields.iterator();
            if (!it.hasNext()) {
                return;
            }
            while (it.hasNext()) {
                FileItem fileItem = it.next();
                boolean isFormField = fileItem.isFormField();
                if (isFormField) {
                    fileData.put(fileItem.getFieldName(), ParameterParserUtil.parseStringParamAndDecode(fileItem.getString("UTF-8"), "", charset));
                } else {
                    file = fileItem;
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }

        /**
         * Parsing and securing all required parameters.
         */
        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        String type = policy.sanitize(fileData.get("type"));
        String system = policy.sanitize(fileData.get("system"));
        String environment = policy.sanitize(fileData.get("environment"));
        String country = policy.sanitize(fileData.get("country"));
        String database = policy.sanitize(fileData.get("database"));
        String databaseUrl = policy.sanitize(fileData.get("databaseUrl"));
        String databaseCsv = policy.sanitize(fileData.get("databaseCsv"));
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String name = fileData.get("name"); //this is mandatory
        String privateData = "true".equals(fileData.get("privateData")) ? "Y" : "N";
        String group = fileData.get("group");
        String description = fileData.get("libdescription");
        String service = fileData.get("service");
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String script = fileData.get("script");
        String servicePath = fileData.get("servicepath");
        String method = fileData.get("method");
        String envelope = fileData.get("envelope");
        String csvUrl = fileData.get("csvUrl");
        String separator = fileData.get("separator");
        String activateAutoSubdata = fileData.get("subdataCheck");
        boolean ignoreFirstLine = ParameterParserUtil.parseBooleanParamAndDecode(fileData.get("ignoreFirstLine"), false, "UTF8");

        Integer testdatalibid = 0;
        boolean testdatalibid_error = true;
        try {
            if (fileData.get("testdatalibid") != null && !fileData.get("testdatalibid").isEmpty()) {
                testdatalibid = Integer.valueOf(fileData.get("testdatalibid"));
                testdatalibid_error = false;
            }
        } catch (NumberFormatException ex) {
            testdatalibid_error = true;
            LOG.warn(ex);
        }

        try {

            // Prepare the final answer.
            MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
            Answer finalAnswer = new Answer(msg1);

            /**
             * Checking all constrains before calling the services.
             */
            if (StringUtil.isEmptyOrNull(name)) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data library")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Test data library name is missing."));
                finalAnswer.setResultMessage(msg);
            } else if (testdatalibid_error) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test data library")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "Could not manage to convert testdatalibid to an integer value or testdatalibid is missing."));
                finalAnswer.setResultMessage(msg);
            } else {
                /**
                 * All data seems cleans so we can call the services.
                 */

                //specific attributes
                ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

                AnswerItem resp = libService.readByKey(testdatalibid);
                if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                    /**
                     * Object could not be found. We stop here and report the
                     * error.
                     */
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, resp);

                } else {
                    /**
                     * The service was able to perform the query and confirm the
                     * object exist, then we can update it.
                     */

                    TestDataLib lib = (TestDataLib) resp.getItem();

                    String fileName = lib.getCsvUrl();
                    if (file != null) {
                        ans = libService.uploadFile(lib.getTestDataLibID(), file);
                        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                            fileName = file.getName();
                        }
                    }

                    lib.setName(name);
                    lib.setType(type);
                    lib.setPrivateData(privateData);
                    lib.setGroup(group);
                    lib.setDescription(description);
                    lib.setSystem(system);
                    lib.setEnvironment(environment);
                    lib.setCountry(country);
                    lib.setDatabase(database);
                    lib.setScript(script);
                    lib.setDatabaseUrl(databaseUrl);
                    lib.setServicePath(servicePath);
                    lib.setService(service);
                    lib.setMethod(method);
                    lib.setEnvelope(envelope);
                    lib.setDatabaseCsv(databaseCsv);
                    lib.setIgnoreFirstLine(ignoreFirstLine);

                    if (file == null) {
                        lib.setCsvUrl(csvUrl);
                    } else {
                        lib.setCsvUrl(File.separator + lib.getTestDataLibID() + File.separator + fileName);
                    }
                    lib.setSeparator(separator);
                    lib.setLastModifier(request.getRemoteUser());

                    ans = libService.update(lib);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);

                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update operation finished with success, then the
                         * logging entry must be added.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createForPrivateCalls("/UpdateTestDataLib", "UPDATE", LogEvent.STATUS_INFO, "Update TestDataLib : ['" + testdatalibid + "'] - name: '" + name + "' system: '"
                                + system + "' environment: '" + environment + "' country: '" + country + "'", request);
                    }

                    List<TestDataLibData> tdldList = new ArrayList<>();
                    // Getting list of SubData from JSON Call
                    if (fileData.get("subDataList") != null) {
                        JSONArray objSubDataArray = new JSONArray(fileData.get("subDataList"));
                        tdldList = getSubDataFromParameter(request, appContext, testdatalibid, objSubDataArray, (file != null && activateAutoSubdata != null && activateAutoSubdata.equals("1")));
                    }

                    // When File has just been uploaded to servlet and flag to load the subdata value has been checked, we will parse it in order to automatically feed the subdata.
                    if (file != null && activateAutoSubdata != null && activateAutoSubdata.equals("1")) {
                        String str = "";
                        String firstLine = "";
                        String secondLine = "";
                        try (BufferedReader reader = new BufferedReader(new FileReader(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_testdatalibfile_path, "", null) + lib.getCsvUrl()));) {
                            firstLine = reader.readLine();
                            secondLine = reader.readLine();
                            String[] firstLineSubData = (!lib.getSeparator().isEmpty()) ? firstLine.split(lib.getSeparator()) : firstLine.split(",");
                            String[] secondLineSubData = (!lib.getSeparator().isEmpty()) ? secondLine.split(lib.getSeparator()) : secondLine.split(",");
                            int i = 0;
                            int y = 1;
                            TestDataLibData firstLineLibData = tdldList.get(0);
                            tdldList = new ArrayList<>();
                            if (StringUtil.isEmptyOrNull(firstLineLibData.getColumnPosition())) {
                                firstLineLibData.setColumnPosition("1");
                            }
                            if (StringUtil.isEmptyOrNull(firstLineLibData.getValue())) {
                                firstLineLibData.setValue(secondLineSubData[0]);
                            }
                            if (StringUtil.isEmptyOrNull(firstLineLibData.getColumn())) {
                                firstLineLibData.setColumn(firstLineSubData[0]);
                            }
                            tdldList.add(firstLineLibData);
                            for (String item : firstLineSubData) {
                                TestDataLibData tdld = tdldFactory.create(null, lib.getTestDataLibID(), item + "_" + y, "N", secondLineSubData[i], item, null, Integer.toString(y), null);
                                tdldList.add(tdld);
                                i++;
                                y++;
                            }

                            // Update the Database with the new list.
                        } finally {
                            try {
                                file.getInputStream().close();
                            } catch (Throwable ignore) {
                            }
                        }
                    } else {
                    }
                    ans = tdldService.compareListAndUpdateInsertDeleteElements(testdatalibid, tdldList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                }

            }
            jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();

        } catch (JSONException ex) {
            LOG.warn(ex);
            //returns a default error message with the json format that is able to be parsed by the client-side
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }

    }

    private List<TestDataLibData> getSubDataFromParameter(HttpServletRequest request, ApplicationContext appContext, int testDataLibId, JSONArray json, boolean onlyFirstLine) throws JSONException {
        List<TestDataLibData> tdldList = new ArrayList<>();
        IFactoryTestDataLibData tdldFactory = appContext.getBean(IFactoryTestDataLibData.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        int maxiteration = json.length();
        if (onlyFirstLine) {
            maxiteration = 1;
        }
        for (int i = 0; i < maxiteration; i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            Integer testDataLibDataId = objectJson.getInt("testDataLibDataID");
            String encrypt = objectJson.getBoolean("encrypt") ? "Y" : "N";
            // Parameter that needs to be secured --> We SECURE+DECODE them
            // NONE
            // Parameter that we cannot secure as we need the html --> We DECODE them
            String subdata = ParameterParserUtil.parseStringParam(objectJson.getString("subData"), "");
            String value = ParameterParserUtil.parseStringParam(objectJson.getString("value"), "");
            String column = ParameterParserUtil.parseStringParam(objectJson.getString("column"), "");
            String parsingAnswer = ParameterParserUtil.parseStringParam(objectJson.getString("parsingAnswer"), "");
            String columnPosition = ParameterParserUtil.parseStringParam(objectJson.getString("columnPosition"), "");
            String description = ParameterParserUtil.parseStringParam(objectJson.getString("description"), "");

            if (!delete) {
                TestDataLibData tdld = tdldFactory.create(testDataLibDataId, testDataLibId, subdata, encrypt, value, column, parsingAnswer, columnPosition, description);
                tdldList.add(tdld);
            }
        }
        return tdldList;
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
