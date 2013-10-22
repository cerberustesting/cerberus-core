package com.redcats.tst.servlet.testCase;

import com.redcats.tst.entity.TCase;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ITestCaseService;
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
 * @author Benoit CIVEL
 * @version 1.0, 07/02/2013
 * @since 2.0.0
 */
@WebServlet(name = "GetTestCaseList", urlPatterns = {"/GetTestCaseList"})
public class GetTestCaseList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String test = policy.sanitize(httpServletRequest.getParameter("test"));
        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        for (TCase testcase : testService.findTestCaseByTest(test)) {
            array.put(testcase.getTestCase());
        }
        try {
            jsonObject.put("testcasesList", array);

            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(jsonObject.toString());
        } catch (JSONException exception) {
            MyLogger.log(GetTestCaseList.class.getName(), Level.WARN, exception.toString());
        }
    }
}