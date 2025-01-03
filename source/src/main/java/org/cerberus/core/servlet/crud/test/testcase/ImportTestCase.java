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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCase;
import org.cerberus.core.crud.service.ITestCaseService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.VersionComparator;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerUtil;
import org.cerberus.core.version.Infos;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "ImportTestCase", urlPatterns = {"/ImportTestCase"})
public class ImportTestCase extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ImportTestCase.class);
    private ITestCaseService testcaseService;

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

                String importOption = param.get("importOption");

                for (Map.Entry<String, String> entry : param.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue();
                    if (key.startsWith("file")) {

                        JSONObject json = new JSONObject(val);

                        if (isCompatible(json)) {

                            //Remove attribute not in the Object
                            JSONObject tcJson;
                            // Testcase moved from ROOT / "testCase" to ROOT / testcases [] in order to support multiple testcase export to a single file. 
                            if (json.has("testCase")) {
                                tcJson = json.getJSONObject("testCase");
                            } else {
                                tcJson = json.getJSONArray("testcases").getJSONObject(0);
                            }
                            JSONArray bugs = tcJson.getJSONArray("bugs");
                            tcJson.remove("bugs");
                            JSONArray condOpts = tcJson.getJSONArray("conditionOptions");
                            tcJson.remove("conditionOptions");

                            if ("2".equalsIgnoreCase(importOption)) {
                                String targetTestcase = testcaseService.getNextAvailableTestcaseId(tcJson.getString("test"));
                                tcJson.put("testcase", targetTestcase);
                            }

                            ObjectMapper mapper = new ObjectMapper();

                            TestCase tcInfo = mapper.readValue(tcJson.toString(), TestCase.class);
                            tcInfo.setBugs(bugs);
                            tcInfo.setConditionOptions(condOpts);
                            LOG.debug(tcInfo.toJson().toString(1));
                            try {
                                testcaseService.createTestcaseWithDependencies(tcInfo);

                                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase " + tcInfo.getTest() + " - " + tcInfo.getTestcase())
                                        .replace("%OPERATION%", "Import"));
                                ans.setResultMessage(msg);
                                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                            } catch (CerberusException ex) {
                                LOG.error("Cerberus Exception during testcase import.", ex);
                                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase " + tcInfo.getTest() + " - " + tcInfo.getTestcase())
                                        .replace("%OPERATION%", "Import")
                                        .replace("%REASON%", ex.getMessageError().getDescription()));
                                ans.setResultMessage(msg);
                                finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
                            }
                        } else {
                            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                            msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase ")
                                    .replace("%OPERATION%", "Import")
                                    .replace("%REASON%", "The file you're trying to import is not supported or is not in a compatible version format."));
                            ans.setResultMessage(msg);
                            finalAnswer = AnswerUtil.agregateAnswer(finalAnswer, ans);
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

    public static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        return new JSONObject(content);
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
                            i++;
                            LOG.debug("Param to import (" + i + ") : " + item.toString() + " | FieldName : " + item.getFieldName() + " | ContentType : " + item.getContentType());
                            if (item.isFormField()) {
                                result.put(item.getFieldName(), item.getString());
                            } else {
                                result.put(item.getFieldName() + i, item.getString());

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

    private boolean isCompatible(JSONObject json) {

        try {
            String fileVersion = json.getString("version");
            String projectVersion = Infos.getInstance().getProjectVersion();
            LOG.debug("Version from import file : " + fileVersion);
            LOG.debug("Current Version of Cerberus : " + projectVersion);

            //Compatibility Matrix. To update if testcase (including dependencies) model change.
            Map<String, String> compatibilityMatrix = new HashMap<>();
            compatibilityMatrix.put("1.0", "4.0");
            compatibilityMatrix.put("4.1", "100.0");

            //Check fileVersion and projectVersion are in the same rank in the compatibility Matrix
            for (Map.Entry<String, String> entry : compatibilityMatrix.entrySet()) {
//                LOG.debug("File - Key : " + VersionComparator.compare(fileVersion, entry.getKey()));
//                LOG.debug("File - Value : " + VersionComparator.compare(fileVersion, entry.getValue()));
//                LOG.debug("Project - Key : " + VersionComparator.compare(projectVersion, entry.getKey()));
//                LOG.debug("Project - Value : " + VersionComparator.compare(projectVersion, entry.getValue()));
                if (VersionComparator.compare(fileVersion, entry.getKey()) * VersionComparator.compare(fileVersion, entry.getValue()) < 0) {
                    return VersionComparator.compare(projectVersion, entry.getKey()) * VersionComparator.compare(projectVersion, entry.getValue()) < 0;
                }
            }
            return false;

        } catch (JSONException ex) {
            LOG.warn(ex);
            return false;
        }
    }

}
