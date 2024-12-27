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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.factory.IFactoryApplicationObject;
import org.cerberus.core.crud.service.IApplicationObjectService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.servlet.ServletUtil;
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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.LogEvent;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "CreateApplicationObject", urlPatterns = {"/CreateApplicationObject"})
public class CreateApplicationObject extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(CreateApplicationObject.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws CerberusException
     * @throws JSONException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);
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
                    fileData.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
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
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String application = ParameterParserUtil.parseStringParamAndDecode(fileData.get("application"), null, charset);
        String object = ParameterParserUtil.parseStringParamAndDecode(fileData.get("object"), null, charset);
        String value = ParameterParserUtil.parseStringParam(fileData.get("value"), null);
        String xOffset = ParameterParserUtil.parseStringParam(fileData.get("xOffset"), null);
        String yOffset = ParameterParserUtil.parseStringParam(fileData.get("yOffset"), null);

        String usrcreated = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getRemoteUser(), "", charset);
        String datecreated = new Timestamp(new java.util.Date().getTime()).toString();
        String usrmodif = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getRemoteUser(), "", charset);
        String datemodif = new Timestamp(new java.util.Date().getTime()).toString();
        // Parameter that we cannot secure as we need the html --> We DECODE them

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isEmptyOrNull(application)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "ApplicationObject")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Application name is missing!"));
            ans.setResultMessage(msg);
        } else if (StringUtil.isEmptyOrNull(object)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "ApplicationObject")
                    .replace("%OPERATION%", "Create")
                    .replace("%REASON%", "Object name is missing!"));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IApplicationObjectService applicationobjectService = appContext.getBean(IApplicationObjectService.class);
            IFactoryApplicationObject factoryApplicationobject = appContext.getBean(IFactoryApplicationObject.class);
            String fileName = "";
            if (file != null) {
                fileName = file.getName();
            }

            ApplicationObject applicationData = factoryApplicationobject.create(-1, application, object, value, fileName, xOffset, yOffset, usrcreated, datecreated, usrmodif, datemodif);
            ans = applicationobjectService.create(applicationData);

            if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                /**
                 * Object created. Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                logEventService.createForPrivateCalls("/CreateApplicationObject", "CREATE", LogEvent.STATUS_INFO, "Create Application Object: ['" + application + "','" + object + "']", request);

                if (file != null) {
                    AnswerItem an = applicationobjectService.readByKey(application, object);
                    if (an.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && an.getItem() != null) {
                        applicationData = (ApplicationObject) an.getItem();
                        ans = applicationobjectService.uploadFile(applicationData.getID(), file);
                        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        }
                    }
                }
            }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

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
            processRequest(request, response);
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
