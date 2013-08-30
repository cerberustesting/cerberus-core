package com.redcats.tst.servlet.engine;

import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ITestCaseExecutionWWWService;
import com.redcats.tst.service.impl.TestCaseExecutionWWWService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import org.apache.log4j.Level;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 01/03/2013
 * @since 2.0.0
 */
@WebServlet(value = "/SaveStatistic")
public class SaveStatistic extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        MyLogger.log(SaveStatistic.class.getName(), Level.DEBUG, "Starting to save statistics Servlet.");
        
        int i = request.getParameter("logId").indexOf('?');
        long runId = Integer.parseInt(request.getParameter("logId").substring(0, i));
        String page = request.getParameter("logId").substring(i).split("=")[1];

        MyLogger.log(SaveStatistic.class.getName(), Level.INFO, " --> save statistics servlet parameters : runid=" + runId + " page=" + page);

        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String str;
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseExecutionWWWService testCaseExecutionWWWService = appContext.getBean(TestCaseExecutionWWWService.class);

        testCaseExecutionWWWService.registerDetail(runId, sb.toString(), page);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
