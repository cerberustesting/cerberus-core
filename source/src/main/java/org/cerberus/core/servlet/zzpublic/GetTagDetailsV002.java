/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.core.servlet.zzpublic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IInvariantService;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionService;
import org.cerberus.core.crud.service.impl.LogEventService;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.ParameterParserUtil;
import java.text.ParseException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.util.StringUtil;

/**
 *
 * @author Nouxx
 * @author vertigo17
 */
@WebServlet(name = "GetTagDetailsV002", urlPatterns = {"/GetTagDetailsV002"})
public class GetTagDetailsV002 extends HttpServlet {

    private ITestCaseExecutionService testCaseExecutionService;
    private IAPIKeyService apiKeyService;
    private ITagService tagService;
    private IParameterService parameterService;
    private IInvariantService invariantService;

    private List<Invariant> prioritiesList = new ArrayList<>();
    private List<Invariant> countriesList = new ArrayList<>();
    private List<Invariant> environmentsList = new ArrayList<>();

    private String cerberusUrlParameter;

    private static final Logger LOG = LogManager.getLogger("GetTagDetailsV002");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        tagService = appContext.getBean(ITagService.class);
        parameterService = appContext.getBean(IParameterService.class);
        invariantService = appContext.getBean(IInvariantService.class);

        String Tag = ParameterParserUtil.parseStringParam(request.getParameter("Tag"), "");
        /**
         * Adding Log entry.
         */
        ILogEventService logEventService = appContext.getBean(LogEventService.class);
        logEventService.createForPublicCalls("/GetTagDetailsV002", "CALL", "GetTagDetails called : " + request.getRequestURL(),
                request);

        apiKeyService = appContext.getBean(IAPIKeyService.class);
        testCaseExecutionService = appContext.getBean(ITestCaseExecutionService.class);
        if (apiKeyService.authenticate(request, response)) {
            List<TestCaseExecution> listOfExecutions;
            List<JSONObject> listOfExecutionsJSON = new ArrayList<>();
            try {
                // get invariants lists (priorities, countries and env)
                prioritiesList = invariantService.readByIdName("PRIORITY");
                countriesList = invariantService.readByIdName("COUNTRY");
                environmentsList = invariantService.readByIdName("ENVIRONMENT");
                JSONObject jsonResponse = new JSONObject();
                Tag tag = tagService.convert(tagService.readByKey(Tag));

                cerberusUrlParameter = parameterService.getParameterStringByKey("cerberus_gui_url", "", "");
                if (StringUtil.isNullOrEmpty(cerberusUrlParameter)) {
                    cerberusUrlParameter = parameterService.getParameterStringByKey("cerberus_url", "", "");
                }

                if (tag != null) {
                    listOfExecutions = testCaseExecutionService.readLastExecutionAndExecutionInQueueByTag(Tag);
                    tag.setExecutionsNew(listOfExecutions);

                    jsonResponse = tag.toJsonV001(cerberusUrlParameter, prioritiesList, countriesList, environmentsList);
                    response.setContentType("application/json");
                    response.getWriter().print(jsonResponse.toString());
                }
            } catch (CerberusException ex) {
                LOG.error(ex.getMessageError().getDescription());
            } catch (ParseException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

}
