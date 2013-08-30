package com.redcats.tst.servlet.application;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.redcats.tst.entity.Application;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IApplicationService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 */
@WebServlet(value = "/GetApplicationList")
public class GetApplicationList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IApplicationService applicationService = appContext.getBean(IApplicationService.class);

        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            for (Application myAppli : applicationService.findAllApplication()) {
                array.put(myAppli.getApplication());
            }
            try {
                jsonObject.put("parameterList", array);

                httpServletResponse.setContentType("application/json");
                httpServletResponse.getWriter().print(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (CerberusException ex) {
            Logger.getLogger(GetApplicationList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
