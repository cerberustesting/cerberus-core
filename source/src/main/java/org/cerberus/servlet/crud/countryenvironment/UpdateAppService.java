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
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceContent;
import org.cerberus.crud.entity.AppServiceHeader;
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
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author cte
 */
public class UpdateAppService extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(UpdateAppService.class);

    private IAppServiceService appServiceService;
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
        String charset = request.getCharacterEncoding();

        // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
        // Parameter that needs to be secured --> We SECURE+DECODE them
        String service = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("service"), null, charset);
        String application = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("application"), null, charset);
        String type = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("type"), null, charset);
        String method = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("method"), "", charset);
        String operation = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("operation"), null, charset);
        String group = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("group"), null, charset);
        String description = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("description"), null, charset);
        // Parameter that we cannot secure as we need the html --> We DECODE them
        String servicePath = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("servicePath"), null, charset);
        String serviceRequest = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("serviceRequest"), null, charset);

        // Prepare the final answer.
        MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
        Answer finalAnswer = new Answer(msg1);

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(service)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "AppService")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "AppService ID (service) is missing."));
            finalAnswer.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            appServiceService = appContext.getBean(IAppServiceService.class);
            appServiceHeaderService = appContext.getBean(IAppServiceHeaderService.class);
            appServiceContentService = appContext.getBean(IAppServiceContentService.class);
            appServiceContentFactory = appContext.getBean(IFactoryAppServiceContent.class);
            appServiceHeaderFactory = appContext.getBean(IFactoryAppServiceHeader.class);

            AnswerItem resp = appServiceService.readByKey(service);
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) resp);

            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can update it.
                 */
                AppService appService = (AppService) resp.getItem();
                appService.setGroup(group);
                appService.setDescription(description);
                appService.setServiceRequest(serviceRequest);
                appService.setOperation(operation);
                appService.setType(type);
                appService.setApplication(application);
                appService.setMethod(method);
                appService.setServicePath(servicePath);
                appService.setUsrModif(request.getRemoteUser());
                ans = appServiceService.update(appService);
                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);

                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                    /**
                     * Update was succesfull. Adding Log entry.
                     */
                    logEventService = appContext.getBean(ILogEventService.class);
                    logEventService.createForPrivateCalls("/UpdateAppService", "UPDATE", "Updated AppService : ['" + service + "']", request);
                }

                // Update content
                if (request.getParameter("contentList") != null) {
                    JSONArray objContentArray = new JSONArray(request.getParameter("contentList"));
                    List<AppServiceContent> contentList = new ArrayList();
                    contentList = getContentListFromRequest(request, appContext, service, objContentArray);

                    // Update the Database with the new list.
                    ans = appServiceContentService.compareListAndUpdateInsertDeleteElements(service, contentList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                }

                // Update header
                if (request.getParameter("headerList") != null) {
                    JSONArray objHeaderArray = new JSONArray(request.getParameter("headerList"));
                    List<AppServiceHeader> headerList = new ArrayList();
                    headerList = getHeaderListFromRequest(request, appContext, service, objHeaderArray);

                    // Update the Database with the new list.
                    ans = appServiceHeaderService.compareListAndUpdateInsertDeleteElements(service, headerList);
                    finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, (Answer) ans);
                }

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
        List<AppServiceContent> contentList = new ArrayList();

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
        List<AppServiceHeader> headerList = new ArrayList();

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
            Logger.getLogger(UpdateAppService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateAppService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            Logger.getLogger(UpdateAppService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateAppService.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
