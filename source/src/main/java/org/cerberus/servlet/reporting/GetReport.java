package org.cerberus.servlet.reporting;

import org.cerberus.entity.TCase;
import org.cerberus.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.ITestCaseExecutionService;
import org.cerberus.service.ITestCaseService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet(name = "GetReport", urlPatterns = {"/GetReport"})
public class GetReport extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = applicationContext.getBean(ITestCaseService.class);
        ITestCaseExecutionService testCaseExecutionService = applicationContext.getBean(ITestCaseExecutionService.class);

        TCase tCase = new TCase();
        tCase.setGroup("AUTOMATED");
        tCase.setApplication("VCCRM");
        tCase.setStatus("WORKING");
        tCase.setPriority(-1);
        List<TCase> list = testCaseService.findTestCaseByAllCriteria(tCase, "", "VC");

        JSONArray data = new JSONArray();
        try {

            for (TCase tc : list) {
                JSONArray object = new JSONArray();
                object.put(tc.getTest());
                object.put(tc.getTestCase());
                object.put(tc.getApplication());
                object.put(tc.getShortDescription());
                object.put(tc.getPriority());
                object.put(tc.getStatus());
//                for (String country : req.getParameterValues("Country[]")) {
                for (String country : req.getParameterValues("Country")) {
//                    for (String browser : req.getParameterValues("Browser[]")) {
                    for (String browser : req.getParameterValues("Browser")) {
                        TestCaseExecution tce = testCaseExecutionService.findLastTCExecutionByCriteria(tc.getTest(), tc.getTestCase(), "", country, "", "", browser, "", "", "", "");
                        if (tce != null) {
                            object.put(tce.getControlStatus());
                            Date date = new Date(tce.getStart());
                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            object.put(formatter.format(date));
                        } else {
                            object.put("");
                            object.put("");
                        }
                    }
                }
                object.put(tc.getComment());
                object.put("for BUILD/REV");
                object.put(tc.getGroup());

                data.put(object);

            }

            JSONObject json = new JSONObject();

            json.put("aaData", data);
            json.put("iTotalRecords", data.length());
            json.put("iTotalDisplayRecords", data.length());
            resp.setContentType("application/json");
            resp.getWriter().print(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
