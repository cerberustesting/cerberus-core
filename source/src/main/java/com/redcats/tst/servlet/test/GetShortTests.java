package com.redcats.tst.servlet.test;

import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ITestService;
import com.redcats.tst.service.impl.TestService;
import org.apache.log4j.Level;
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

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 07/02/2013
 * @since 2.0.0
 */
@WebServlet(value = "/GetShortTests")
public class GetShortTests extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestService testService = appContext.getBean(TestService.class);

        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        for (String test : testService.getListOfTests()) {
            array.put(test);
        }
        try {
            jsonObject.put("testsList", array);

            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().print(jsonObject.toString());
        } catch (JSONException exception) {
            MyLogger.log(GetShortTests.class.getName(), Level.WARN, exception.toString());
        }
    }
}
