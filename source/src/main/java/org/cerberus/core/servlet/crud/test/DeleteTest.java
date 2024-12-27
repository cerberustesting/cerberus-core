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
package org.cerberus.core.servlet.crud.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Test;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseStepService;
import org.cerberus.core.crud.service.ITestService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
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

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.cerberus.core.crud.entity.LogEvent;

/**
 * @author cerberus
 */
@WebServlet(name = "DeleteTest", urlPatterns = {"/DeleteTest"})
public class DeleteTest extends HttpServlet {

    private static Logger LOGGER = LogManager.getLogger(DeleteTest.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException {
        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        // Parsing and securing all required parameters.
        String key = policy.sanitize(request.getParameter("test"));

        // Checking all constrains before calling the services.
        if (StringUtil.isEmptyOrNULLString(key)) {
            ans.setResultMessage(
                    new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED)
                            .resolveDescription("ITEM", "Test")
                            .resolveDescription("OPERATION", "Delete")
                            .resolveDescription("REASON", "Test name is missing.")
            );
        } else {
            // All data seems cleans so we can call the services.
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestService testService = appContext.getBean(ITestService.class);
            IParameterService parameterService = appContext.getBean(IParameterService.class);

            AnswerItem resp = testService.readByKey(key);

            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem() != null)) {
                // Object could not be found. We stop here and report the error.
                ans.setResultMessage(
                        new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED)
                                .resolveDescription("ITEM", "Test")
                                .resolveDescription("OPERATION", "Delete")
                                .resolveDescription("REASON", "Test does not exist")
                );
            } else {
                // The service was able to perform the query and confirm the object exist
                Test testData = (Test) resp.getItem();

                // Check if there is no associated Test Cases defining Step which is used OUTSIDE of the deleting Test
                try {
                    final Collection<TestCaseStep> externallyUsedTestCaseSteps = externallyUsedTestCaseSteps(testData);
                    if (!externallyUsedTestCaseSteps.isEmpty()) {
                        String cerberusUrlTemp = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
                        if (StringUtil.isEmptyOrNull(cerberusUrlTemp)) {
                            cerberusUrlTemp = parameterService.getParameterStringByKey("cerberus_url", "", "");
                        }
                        final String cerberusUrl = cerberusUrlTemp;

//                        final String cerberusUrl = appContext.getBean(IParameterService.class).findParameterByKey("cerberus_url", "").getValue();
                        ans.setResultMessage(
                                new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED)
                                        .resolveDescription("ITEM", "Test")
                                        .resolveDescription("OPERATION", "Delete")
                                        .resolveDescription(
                                                "REASON", "You are trying to remove a Test which contains Test Case Steps which are currently used by other Test Case Steps outside of the removing Test. Please remove this link before to proceed: "
                                                        + Collections2.transform(externallyUsedTestCaseSteps, (@Nullable final TestCaseStep input) -> String.format(
                                                        "<a href='%s/TestCaseScript.jsp?test=%s&testcase=%s&step=%s'>%s/%s#%s</a>",
                                                        cerberusUrl,
                                                        input.getTest(),
                                                        input.getTestcase(),
                                                        input.getStepId(),
                                                        input.getTest(),
                                                        input.getTestcase(),
                                                        input.getStepId()
                                                ))
                                        )
                        );
                    } else {
                        // Test seems clean, process to delete
                        ans = testService.delete(testData);

                        if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                            // Delete was successful. Adding Log entry.
                            ILogEventService logEventService = appContext.getBean(LogEventService.class);
                            logEventService.createForPrivateCalls("/DeleteTest", "DELETE", LogEvent.STATUS_INFO, "Delete Test : ['" + key + "']", request);
                        }
                    }
                } catch (final CerberusException e) {
                    LOGGER.error(e.getMessage(), e);
                    ans.setResultMessage(new MessageEvent(
                            MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED)
                            .resolveDescription("DESCRIPTION", "Unexpected error: " + e.getMessage())
                    );
                }
            }
        }

        // Formating and returning the json result.
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse.toString());
        response.getWriter().flush();
    }

    /**
     * Get {@link TestCaseStep} which are using an other {@link TestCaseStep}
     * from the given {@link Test} but which are NOT included into this
     * {@link Test}
     *
     * @param test the {@link Test} from which getting externally used
     *             {@link TestCaseStep}s
     * @return a {@link Collection} of {@link TestCaseStep} which are using an
     * other {@link TestCaseStep} from the given {@link Test} but which are NOT
     * included into this {@link Test}
     * @throws CerberusException if an unexpected error occurred
     */
    private Collection<TestCaseStep> externallyUsedTestCaseSteps(final Test test) throws CerberusException {
        // Get the associated ApplicationContext to this servlet
        final ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        // Get all TestCaseSteps which are using an other TestCaseSteps from given Test
        final ITestCaseStepService testCaseStepService = applicationContext.getBean(ITestCaseStepService.class);
        final List<TestCaseStep> stepsInUse = testCaseStepService.getTestCaseStepsUsingTestInParameter(test.getTest());

        // Filter the retrieved list to only retain those which are not included from the given Test
        return Collections2.filter(stepsInUse, new Predicate<TestCaseStep>() {
            @Override
            public boolean apply(@Nullable final TestCaseStep input) {
                return !input.getTest().equals(test.getTest());
            }

            @Override
            public boolean test(TestCaseStep t) {
                return Predicate.super.test(t); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            LOGGER.error(ex.getMessage(), ex);
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
