package com.redcats.tst.servlet.testCase;

import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ITestCaseCountryService;
import com.redcats.tst.service.ITestCaseService;
import com.redcats.tst.service.impl.TestCaseService;
import org.apache.log4j.Level;
import org.json.JSONArray;
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

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 21/02/2013
 * @since 2.0.0
 */
@WebServlet(value = "/GetCountryForTestCase")
public class GetCountryForTestCase extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        String testName = policy.sanitize(httpServletRequest.getParameter("test"));
        String testCaseName = policy.sanitize(httpServletRequest.getParameter("testCase"));

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testCaseService = appContext.getBean(TestCaseService.class);
        ITestCaseCountryService testCaseCountryService = appContext.getBean(ITestCaseCountryService.class);

        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        for (String country : testCaseCountryService.findListOfCountryByTestTestCase(testName, testCaseName)) {
            array.put(country);
        }
        try {
            jsonObject.put("countriesList", array);

            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(jsonObject.toString());
        } catch (JSONException exception) {
            MyLogger.log(GetCountryForTestCase.class.getName(), Level.WARN, exception.toString());
        }
    }
}
