/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.parameter;

import com.redcats.tst.entity.Parameter;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IParameterService;
import com.redcats.tst.service.IUserService;
import com.redcats.tst.service.impl.ParameterService;
import com.redcats.tst.service.impl.UserService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author ip100003
 */
@WebServlet(name = "GetParameter", urlPatterns = {"/GetParameter"})
public class GetParameter extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String echo = request.getParameter("sEcho");

        JSONArray data = new JSONArray(); //data that will be shown in the table

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IParameterService parameterService = appContext.getBean(ParameterService.class);
        try {
            JSONObject jsonResponse = new JSONObject();
            try {
                for (Parameter myParameter : parameterService.findAllParameter()) {
                    JSONArray row = new JSONArray();
                    row.put(myParameter.getParam()).put(myParameter.getValue()).put(myParameter.getDescription());
                    data.put(row);
                }
            } catch (CerberusException ex) {
                response.setContentType("text/html");
                response.getWriter().print(ex.getMessageError().getDescription());

            }
            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", data.length());
            jsonResponse.put("iTotalDisplayRecords", data.length());
            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException e) {
            MyLogger.log(GetParameter.class.getName(), Level.FATAL, "" + e);
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
        }
    }
}
