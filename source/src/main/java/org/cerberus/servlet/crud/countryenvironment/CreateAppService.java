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
package org.cerberus.servlet.crud.countryenvironment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
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
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.factory.IFactoryAppServiceContent;
import org.cerberus.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.crud.service.IAppServiceContentService;
import org.cerberus.crud.service.IAppServiceHeaderService;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author cte
 */
public class CreateAppService extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateAppService.class);

    private IAppServiceService appServiceService;
    private IFactoryAppService appServiceFactory;
    private IAppServiceHeaderService appServiceHeaderService;
    private IAppServiceContentService appServiceContentService;
    private ILogEventService logEventService;
    private IFactoryAppServiceContent appServiceContentFactory;
    private IFactoryAppServiceHeader appServiceHeaderFactory;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    final void processRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("text/html;charset=UTF-8");
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        Map<String, String> fileData = new HashMap<String, String>();
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
                    fileData.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
                } else {
                    file = fileItem;
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String service = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("service"), null, charset);
        String group = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("group"), "", charset);
        String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("description"), "", charset);
        String attachementurl = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("attachementurl"), "", charset);
        String operation = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("operation"), "", charset);
        String application = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("application"), null, charset);
        String type = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("type"), "", charset);
        String method = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(fileData.get("method"), "", charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String servicePath = ParameterParserUtil.parseStringParamAndDecode(fileData.get("servicePath"), "", charset);
        String serviceRequest = ParameterParserUtil.parseStringParamAndDecode(fileData.get("srvRequest"), null, charset);
        String kafkaTopic = ParameterParserUtil.parseStringParamAndDecode(fileData.get("kafkaTopic"), "", charset);
        String kafkaKey = ParameterParserUtil.parseStringParamAndDecode(fileData.get("kafkaKey"), "", charset);
        String kafkaFilterPath = ParameterParserUtil.parseStringParamAndDecode(fileData.get("kafkaFilterPath"), "", charset);
        String kafkaFilterValue = ParameterParserUtil.parseStringParamAndDecode(fileData.get("kafkaFilterValue"), "", charset);
        String fileName = null;
        if (file != null) {
            fileName = file.getName();
        }

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(service)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "AppService")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Service name is missing!"));
            finalAnswer.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */

            appServiceService = appContext.getBean(IAppServiceService.class);
            appServiceFactory = appContext.getBean(IFactoryAppService.class);
            appServiceHeaderService = appContext.getBean(IAppServiceHeaderService.class);
            appServiceContentService = appContext.getBean(IAppServiceContentService.class);
            appServiceContentFactory = appContext.getBean(IFactoryAppServiceContent.class);
            appServiceHeaderFactory = appContext.getBean(IFactoryAppServiceHeader.class);

            AppService appService = appServiceFactory.create(service, type, method, application, group, serviceRequest, kafkaTopic, kafkaKey, kafkaFilterPath, kafkaFilterValue, description, servicePath, attachementurl, operation, request.getRemoteUser(), null, null, null, fileName);
            ans = appServiceService.create(appService);
            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Adding Log entry.
                 */
                logEventService = appContext.getBean(ILogEventService.class);
                logEventService.createForPrivateCalls("/CreateAppService", "CREATE", "Create AppService : ['" + service + "']", request);

                if (file != null) {
                    AppService an = appServiceService.findAppServiceByKey(service);
                    if (an != null) {
                        appServiceService.uploadFile(an.getService(), file);
                    }
                }
            }
            // Update content
            if (fileData.get("contentList") != null) {
                JSONArray objContentArray = new JSONArray(fileData.get("contentList"));
                List<AppServiceContent> contentList = new ArrayList<>();
                contentList = getContentListFromRequest(request, appContext, service, objContentArray);

                // Update the Database with the new list.
                ans = appServiceContentService.compareListAndUpdateInsertDeleteElements(service, contentList);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
            }
            // Update header
            if (fileData.get("headerList") != null) {
                JSONArray objHeaderArray = new JSONArray(fileData.get("headerList"));
                List<AppServiceHeader> headerList = new ArrayList<>();
                headerList = getHeaderListFromRequest(request, appContext, service, objHeaderArray);

                // Update the Database with the new list.
                ans = appServiceHeaderService.compareListAndUpdateInsertDeleteElements(service, headerList);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();
    }

    private List<AppServiceContent> getContentListFromRequest(HttpServletRequest request, ApplicationContext appContext, String service, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<AppServiceContent> contentList = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            int sort = objectJson.getInt("sort");
            String key = objectJson.getString("key");
            String value = objectJson.getString("value");
            String active = objectJson.getString("active");
            String description = objectJson.getString("description");

            if (!delete) {
                contentList.add(appServiceContentFactory.create(service, key, value, active, sort, description, request.getRemoteUser(), null, request.getRemoteUser(), null));
            }
        }
        return contentList;
    }

    private List<AppServiceHeader> getHeaderListFromRequest(HttpServletRequest request, ApplicationContext appContext, String service, JSONArray json) throws CerberusException, JSONException, UnsupportedEncodingException {
        List<AppServiceHeader> headerList = new ArrayList<>();

        for (int i = 0; i < json.length(); i++) {
            JSONObject objectJson = json.getJSONObject(i);

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            boolean delete = objectJson.getBoolean("toDelete");
            int sort = objectJson.getInt("sort");
            String key = objectJson.getString("key");
            String value = objectJson.getString("value");
            String active = objectJson.getString("active");
            String description = objectJson.getString("description");

            if (!delete) {
                headerList.add(appServiceHeaderFactory.create(service, key, value, active, sort, description, request.getRemoteUser(), null, request.getRemoteUser(), null));
            }
        }
        return headerList;
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
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
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
            this.processRequest(request, response);
        } catch (CerberusException ex) {
            LOG.warn(ex);
        } catch (JSONException ex) {
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
