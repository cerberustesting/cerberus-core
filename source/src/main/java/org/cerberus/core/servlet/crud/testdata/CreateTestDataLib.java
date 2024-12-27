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
import org.cerberus.core.crud.factory.IFactoryTestDataLib;
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
 * Servlet responsible for handling the creation of new test data lib entries
 *
 * @author FNogueira
 */
@WebServlet(name = "CreateTestDataLib", urlPatterns = {"/CreateTestDataLib"})
public class CreateTestDataLib extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateTestDataLib.class);

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

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IFactoryTestDataLibData tdldFactory = appContext.getBean(IFactoryTestDataLibData.class);
        ITestDataLibDataService tdldService = appContext.getBean(ITestDataLibDataService.class);
        IParameterService parameterService = appContext.getBean(IParameterService.class);

        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        AnswerItem ansItem = new AnswerItem<>();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

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
            LOG.warn(e, e);
        } catch (Exception e) {
            LOG.warn(e, e);
        }

        try {

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

            /**
             * Checking all constrains before calling the services.
             */
            // Prepare the final answer.
            MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
            Answer finalAnswer = new Answer(msg1);

            if (StringUtil.isEmptyOrNull(name)) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                        .replace("%OPERATION%", "Create")
                        .replace("%REASON%", "Test data library name is missing! "));
                finalAnswer.setResultMessage(msg);

            } else {
                /**
                 * All data seems cleans so we can call the services.
                 */
                ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);
                IFactoryTestDataLib factoryLibService = appContext.getBean(IFactoryTestDataLib.class);

                TestDataLib lib = factoryLibService.create(0, name, system, environment, country, privateData, group,
                        type, database, script, databaseUrl, service, servicePath, method, envelope, databaseCsv, csvUrl, separator, ignoreFirstLine, description,
                        request.getRemoteUser(), null, "", null, null, null, null, null);

                //Creates the entries and the subdata list
                ansItem = libService.create(lib);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ansItem);

                /**
                 * Object created. Adding Log entry.
                 */
                if (ansItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    ILogEventService logEventService = appContext.getBean(LogEventService.class);
                    logEventService.createForPrivateCalls("/CreateTestDataLib", "CREATE", LogEvent.STATUS_INFO, "Create TestDataLib  : ['" + name + "']", request);
                }

                List<TestDataLibData> tdldList = new ArrayList<>();
                TestDataLib dataLibWithUploadedFile = (TestDataLib) ansItem.getItem();

                if (file != null) {
                    String fileName;
                    ans = libService.uploadFile(dataLibWithUploadedFile.getTestDataLibID(), file);
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        fileName = file.getName();
                        dataLibWithUploadedFile.setCsvUrl(File.separator + dataLibWithUploadedFile.getTestDataLibID() + File.separator + fileName);
                        lib.setTestDataLibID(dataLibWithUploadedFile.getTestDataLibID());
                        ans = libService.update(lib);
                        finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                    }
                }

                // Getting list of SubData from JSON Call
                if (fileData.get("subDataList") != null) {
                    JSONArray objSubDataArray = new JSONArray(fileData.get("subDataList"));
                    tdldList = getSubDataFromParameter(request, appContext, dataLibWithUploadedFile.getTestDataLibID(), objSubDataArray, (file != null && activateAutoSubdata != null && activateAutoSubdata.equals("1")));
                }

                if (file != null && activateAutoSubdata != null && activateAutoSubdata.equals("1")) {
                    String firstLine = "";
                    String secondLine = "";
                    try (BufferedReader reader = new BufferedReader(new FileReader(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_testdatalibfile_path, "", null) + dataLibWithUploadedFile.getCsvUrl()));) {
                        firstLine = reader.readLine();
                        secondLine = reader.readLine();
                        LOG.debug(firstLine);
                        LOG.debug(secondLine);
                        String[] firstLineSubData = (!dataLibWithUploadedFile.getSeparator().isEmpty()) ? firstLine.split(dataLibWithUploadedFile.getSeparator()) : firstLine.split(",");
                        String[] secondLineSubData = (!dataLibWithUploadedFile.getSeparator().isEmpty()) ? secondLine.split(dataLibWithUploadedFile.getSeparator()) : secondLine.split(",");
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
                            TestDataLibData tdld = tdldFactory.create(null, dataLibWithUploadedFile.getTestDataLibID(), item + "_" + y, "N", secondLineSubData[i], item, null, Integer.toString(y), null);
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

                ans = tdldService.compareListAndUpdateInsertDeleteElements(dataLibWithUploadedFile.getTestDataLibID(), tdldList);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
            }

            /**
             * Formating and returning the json result.
             */
            //sets the message returned by the operations
            jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());
            response.getWriter().print(jsonResponse);
            response.getWriter().flush();
        } catch (JSONException ex) {
            LOG.warn(ex, ex);
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            response.getWriter().flush();
        } catch (Exception e) {
            LOG.warn(e, e);
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
            // Parameter that needs to be secured --> We SECURE+DECODE them
            // NONE
            // Parameter that we cannot secure as we need the html --> We DECODE them
            String subdata = ParameterParserUtil.parseStringParam(objectJson.getString("subData"), "");
            String encrypt = objectJson.getBoolean("encrypt") ? "Y" : "N";
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
