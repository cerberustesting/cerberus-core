/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import org.json.JSONArray;
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

/**
 * @author bcivel
 */
@WebServlet(name = "TestCaseActionExecutionDetail", urlPatterns = {"/TestCaseActionExecutionDetail"})
public class TestCaseActionExecutionDetail extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseExecutionDetailService testCaseExecutionDetailService = appContext.getBean(ITestCaseExecutionDetailService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String test = policy.sanitize(httpServletRequest.getParameter("test"));
        String testcase = policy.sanitize(httpServletRequest.getParameter("testcase"));
        String country = policy.sanitize(httpServletRequest.getParameter("country"));


        JSONArray data = testCaseExecutionDetailService.lastActionExecutionDuration(test, testcase, country);

        try {


            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(data.toString());
        } catch (Exception e) {
            httpServletResponse.setContentType("text/html");
            httpServletResponse.getWriter().print(e.getMessage());
        }
    }
}