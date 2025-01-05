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
package org.cerberus.core.servlet.crud.test.testcase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.CountryEnvironmentParameters;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.factory.IFactoryTestCase;
import org.cerberus.core.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.core.crud.factory.IFactoryTestCaseStep;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.core.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.core.crud.service.IApplicationService;
import org.cerberus.core.crud.service.ICountryEnvironmentParametersService;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.xml.sax.SAXException;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ImportTestCaseFromTestLink", urlPatterns = {"/ImportTestCaseFromTestLink"})
public class ImportTestCaseFromTestLink extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ImportTestCaseFromTestLink.class);
    private ITestCaseService testcaseService;
    private IApplicationService applicationService;
    private ICountryEnvironmentParametersService countryEnvironmentParametersService;
    private IInvariantService invariantService;
    private IFactoryTestCase testcaseFactory;
    private IFactoryTestCaseCountry testcaseCountryFactory;
    private IFactoryTestCaseStep testcaseStepFactory;
    private IFactoryTestCaseStepAction testcaseStepActionFactory;
    private IFactoryTestCaseStepActionControl testcaseStepActionControlFactory;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

        JSONObject jsonResponse = new JSONObject();
        try {
            try {
                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
                testcaseService = appContext.getBean(ITestCaseService.class);
                testcaseFactory = appContext.getBean(IFactoryTestCase.class);
                testcaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);
                testcaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);
                testcaseStepActionControlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);
                applicationService = appContext.getBean(IApplicationService.class);
                invariantService = appContext.getBean(IInvariantService.class);
                testcaseCountryFactory = appContext.getBean(IFactoryTestCaseCountry.class);
                countryEnvironmentParametersService = appContext.getBean(ICountryEnvironmentParametersService.class);

                Answer ans = new Answer();
                MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
                ans.setResultMessage(msg);

                ///Get files
//                List<String> files = getFiles(httpServletRequest);
                HashMap<String, String> param = getParams(httpServletRequest);
                String userCreated = httpServletRequest.getUserPrincipal().getName();

                // Prepare the final answer.
                MessageEvent msg1 = new MessageEvent(MessageEventEnum.GENERIC_OK);
                Answer finalAnswer = new Answer(msg1);

                String targetFolder = param.get("test");
                String targetApplication = param.get("application");

                LOG.debug("Requested Test Folder : " + targetFolder);
                LOG.debug("Requested Test Application : " + targetApplication);

                List<Invariant> countries = invariantService.readByIdName("COUNTRY");
                Application app = applicationService.convert(applicationService.readByKey(targetApplication));
                List<CountryEnvironmentParameters> envParams = countryEnvironmentParametersService.convert(countryEnvironmentParametersService.readByVarious(null, null, null, targetApplication));
                List<String> urls = new ArrayList<>();
                for (CountryEnvironmentParameters envParam : envParams) {
                    urls.add(envParam.getIp());
                }

                for (Map.Entry<String, String> entry : param.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue();
                    if (key.startsWith("file")) {

                        LOG.debug(val);

                        SAXParserFactory factory = SAXParserFactory.newInstance();

                        try (InputStream is = new ByteArrayInputStream(val.getBytes())) {

                            SAXParser saxParser = factory.newSAXParser();

                            // parse XML and map to object,
                            MapTestLinkObjectHandlerSax handler = new MapTestLinkObjectHandlerSax(targetFolder, targetApplication, userCreated, testcaseService, invariantService);

                            saxParser.parse(is, handler);

                            // print all
                            List<TestCase> result = handler.getResult();

                            for (TestCase testCase : result) {
                                testcaseService.createTestcaseWithDependenciesAPI(testCase);
                            }

                        } catch (ParserConfigurationException | SAXException | IOException e) {
                            LOG.error(e, e);
                        }

                    }
                }

                jsonResponse.put("messageType", finalAnswer.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", finalAnswer.getResultMessage().getDescription());

            } catch (Exception ex) {
                jsonResponse.put("messageType", MessageEventEnum.GENERIC_ERROR.getCodeString());
                jsonResponse.put("message", MessageEventEnum.GENERIC_ERROR.getDescription().replace("%REASON%", ex.toString()));
                LOG.error("General Exception during testcase import.", ex);
            }
        } catch (JSONException e) {
            LOG.error("JSONException during testcase import.", e);
            //returns a default error message with the json format that is able to be parsed by the client-side
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(AnswerUtil.createGenericErrorAnswer());
        }
        httpServletResponse.getWriter().print(jsonResponse.toString());
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

    private HashMap<String, String> getParams(HttpServletRequest httpServletRequest) {
        HashMap<String, String> result = new HashMap<>();

        try {
            if (ServletFileUpload.isMultipartContent(httpServletRequest)) {
                DiskFileItemFactory factory = new DiskFileItemFactory();

                ServletContext servletContext = this.getServletConfig().getServletContext();
                File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                factory.setRepository(repository);

                ServletFileUpload upload = new ServletFileUpload(factory);

                List<FileItem> formItems = upload.parseRequest(httpServletRequest);
                if (formItems != null) {
                    LOG.debug("Nb of Param to import : " + formItems.size());
                    if (formItems.size() > 0) {
                        int i = 1;
                        for (FileItem item : formItems) {
                            LOG.debug("Param to import (" + i++ + ") : " + item.toString() + " FieldName : " + item.getFieldName() + " ContentType : " + item.getContentType());
                            if (item.isFormField()) {
                                result.put(item.getFieldName(), item.getString());
                            } else {
                                try {
                                    result.put(item.getFieldName() + i, item.getString("utf-8"));
                                } catch (UnsupportedEncodingException ex) {
                                    LOG.warn(ex, ex);
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileUploadException ex) {
            LOG.error(ex, ex);
        }
        LOG.debug("result Param : " + result.size());
        return result;
    }

}
