/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.redcats.tst.servlet.execution;

import com.redcats.tst.entity.*;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryTCExecution;
import com.redcats.tst.factory.IFactoryTestCaseExecutionSysVer;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.*;
import org.apache.log4j.Level;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/10/2013
 * @since 0.9.1
 */
@WebServlet(name = "SaveManualExecution", urlPatterns = {"/SaveManualExecution"})
public class SaveManualExecution extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String test = req.getParameter("test");
        String testCase = req.getParameter("testCase");
        String env = req.getParameter("env");
        String country = req.getParameter("country");
        String controlStatus = req.getParameter("controlStatus");
        String controlMessage = req.getParameter("controlMessage");
        String tag = req.getParameter("tag");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseService testService = appContext.getBean(ITestCaseService.class);
        IApplicationService applicationService = appContext.getBean(IApplicationService.class);
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        ITestCaseExecutionSysVerService testCaseExecutionSysVerService = appContext.getBean(ITestCaseExecutionSysVerService.class);
        ICountryEnvParamService countryEnvParamService = appContext.getBean(ICountryEnvParamService.class);

        IFactoryTCExecution factoryTCExecution = appContext.getBean(IFactoryTCExecution.class);
        IFactoryTestCaseExecutionSysVer factoryTestCaseExecutionSysVer = appContext.getBean(IFactoryTestCaseExecutionSysVer.class);

        try {
            TCase tCase = testService.findTestCaseByKey(test, testCase);
            Application application = applicationService.findApplicationByKey(tCase.getApplication());
            CountryEnvParam countryEnvParam = null;
            try {
                countryEnvParam = countryEnvParamService.findCountryEnvParamByKey(application.getSystem(), country, env);
            } catch (CerberusException e) {
                CerberusException ex = new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
                ex.getMessageError().setDescription("Combination Environment: '" + env + "' and Country: '" + country
                        + "' not defined for System/Application: " + application.getSystem() + "/" + application.getApplication());
                throw ex;
            }
            String build = countryEnvParam.getBuild();
            String revision = countryEnvParam.getRevision();
            long now = new Date().getTime();
            String version = "";

            TCExecution execution = factoryTCExecution.create(0, test, testCase, build, revision, env, country, "", now, now,
                    controlStatus, controlMessage, application, "", "", "", tag, "Y", 0, 0, "", tCase.getStatus(), version,
                    null, null, null, false, "", "", "", "", "", "", null, null);

            testCaseExecutionService.insertTCExecution(execution);

            TestCaseExecutionSysVer testCaseExecutionSysVer = factoryTestCaseExecutionSysVer.create(execution.getId(), application.getSystem(), build, revision);
            testCaseExecutionSysVerService.insertTestCaseExecutionSysVer(testCaseExecutionSysVer);

        } catch (CerberusException e) {
            MyLogger.log(SaveManualExecution.class.getName(), Level.FATAL, "" + e.getMessageError().getDescription());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/html");
            resp.getWriter().print(e.getMessageError().getDescription());
        }
    }
}
