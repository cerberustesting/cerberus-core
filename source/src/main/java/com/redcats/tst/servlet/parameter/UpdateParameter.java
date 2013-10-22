/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.parameter;

import com.redcats.tst.entity.Parameter;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.IParameterService;
import com.redcats.tst.service.impl.LogEventService;
import com.redcats.tst.service.impl.ParameterService;
import com.redcats.tst.service.impl.UserService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author ip100003
 */
@WebServlet(name = "UpdateParameter", urlPatterns = {"/UpdateParameter"})
public class UpdateParameter extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //TODO create class Validator to validate all parameter from page
        String param = request.getParameter("id");
        int columnPosition = Integer.parseInt(request.getParameter("columnPosition"));
        String value = request.getParameter("value").replaceAll("'", "");

        MyLogger.log(UpdateParameter.class.getName(), Level.INFO, "value : " + value + " columnPosition : " + columnPosition + " param : " + param);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IParameterService parameterService = appContext.getBean(ParameterService.class);

        Parameter myParameter;
        try {
            myParameter = parameterService.findParameterByKey(param);
            switch (columnPosition) {
                case 1:
                    myParameter.setValue(value);
                    break;
            }
            try {
                parameterService.updateParameter(myParameter);

                /**
                 * Adding Log entry.
                 */
                ILogEventService logEventService = appContext.getBean(LogEventService.class);
                IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
                try {
                    logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/UpdateParameterAjax", "UPDATE", "Update parameter : " + param, "", ""));
                } catch (CerberusException ex) {
                    Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
                }

                response.getWriter().print(value);
            } catch (CerberusException ex) {
                response.getWriter().print(ex.getMessageError().getDescription());
            }
        } catch (CerberusException ex) {
            response.getWriter().print(ex.getMessageError().getDescription());
        }

    }
}
