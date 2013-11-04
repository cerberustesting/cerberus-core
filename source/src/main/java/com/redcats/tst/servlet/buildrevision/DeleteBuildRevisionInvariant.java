/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.servlet.buildrevision;

import com.redcats.tst.entity.BuildRevisionInvariant;
import com.redcats.tst.servlet.user.*;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryBuildRevisionInvariant;
import com.redcats.tst.factory.IFactoryGroup;
import com.redcats.tst.factory.IFactoryLogEvent;
import com.redcats.tst.factory.impl.FactoryBuildRevisionInvariant;
import com.redcats.tst.factory.impl.FactoryGroup;
import com.redcats.tst.factory.impl.FactoryLogEvent;
import com.redcats.tst.service.IBuildRevisionInvariantService;
import com.redcats.tst.service.ILogEventService;
import com.redcats.tst.service.IUserService;
import com.redcats.tst.service.impl.BuildRevisionInvariantService;
import com.redcats.tst.service.impl.LogEventService;
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
 * @author ip100003
 */
@WebServlet(name = "DeleteBuildRevisionInvariant", urlPatterns = {"/DeleteBuildRevisionInvariant"})
public class DeleteBuildRevisionInvariant extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String system = request.getParameter("system");
        Integer level = Integer.valueOf(request.getParameter("level"));
        Integer seq = Integer.valueOf(request.getParameter("seq"));

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IBuildRevisionInvariantService briService = appContext.getBean(BuildRevisionInvariantService.class);
        IFactoryBuildRevisionInvariant factoryBuildRevisionInvariant = new FactoryBuildRevisionInvariant();

        BuildRevisionInvariant myBRI = factoryBuildRevisionInvariant.create(system, level, seq, "");
        if (briService.deleteBuildRevisionInvariant(myBRI)) {

            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            try {
                logEventService.insertLogEvent(factoryLogEvent.create(0, 0, request.getUserPrincipal().getName(), null, "/DeleteBuildRevisionInvariant", "DELETE", "Delete buildRevisionInvariant : " + system + "-" + level + "-" + seq, "",""));
            } catch (CerberusException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.ERROR, null, ex);
            }

        } else {
            response.getWriter().print("Could not Delete Build Revision : " + system + "-" + level + "-" + seq );

        }


    }
}