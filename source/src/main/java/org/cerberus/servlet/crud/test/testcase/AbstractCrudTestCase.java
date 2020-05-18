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
package org.cerberus.servlet.crud.test.testcase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.engine.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.json.JSONException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.json.JSONArray;

public abstract class AbstractCrudTestCase extends HttpServlet {

    private WebApplicationContext springContext;

    private static final Logger LOG = LogManager.getLogger(AbstractCrudTestCase.class);

    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, CerberusException, JSONException;

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException | JSONException ex) {
            LOG.warn(ex, ex);
        } // FIXME where Exception is managed ?
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException | JSONException ex) {
            LOG.warn(ex, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }

    protected TestCase getTestCaseFromRequest(HttpServletRequest request, TestCase tc) throws CerberusException {
        try {

            String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();

            // Parameter that are already controled by GUI (no need to decode) --> We SECURE them
            tc.setImplementer(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("implementer"), tc.getImplementer(), charset));
            tc.setExecutor(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("executor"), tc.getExecutor(), charset));
            tc.setExecutor(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("executor"), tc.getExecutor(), charset));
            tc.setUsrCreated(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getUserPrincipal().getName(), "", charset));
            tc.setApplication(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("application"), tc.getApplication(), charset));
            tc.setActiveQA(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeQA"), tc.getActiveQA(), charset));
            tc.setActiveUAT(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeUAT"), tc.getActiveUAT(), charset));
            tc.setActivePROD(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("activeProd"), tc.getActivePROD(), charset));
            tc.setFromBuild(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("fromSprint"), tc.getFromBuild(), charset));
            tc.setFromRev(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("fromRev"), tc.getFromRev(), charset));
            tc.setToBuild(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("toSprint"), tc.getToBuild(), charset));
            tc.setToRev(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("toRev"), tc.getToRev(), charset));
            tc.setTcActive(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("active"), tc.getTcActive(), charset));
            tc.setTargetBuild(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("targetSprint"), tc.getTargetBuild(), charset));
            tc.setTargetRev(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("targetRev"), tc.getTargetRev(), charset));
            tc.setPriority(ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("priority"), tc.getPriority(), charset));
            tc.setTest(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), tc.getTest(), charset));
            tc.setTestCase(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testCase"), tc.getTestCase(), charset));
            tc.setOrigine(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("origin"), tc.getOrigine(), charset));
            tc.setType(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("group"), tc.getType(), charset));
            tc.setStatus(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("status"), tc.getStatus(), charset));
            tc.setDescription(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("shortDesc"), tc.getDescription(), charset));
            String bug = tc.getBugID() == null ? "" : tc.getBugID().toString();
            String bugIDString = ParameterParserUtil.parseStringParamAndDecode(request.getParameter("bugId"), bug, charset);
            JSONArray bugID = new JSONArray();
            try {
                bugID = new JSONArray(bugIDString);
            } catch (JSONException ex) {
                LOG.error("Could not convert '" + bugIDString + "' to JSONArray.", ex);
            }
            tc.setBugID(bugID);
            tc.setComment(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("comment"), tc.getComment(), charset));
            tc.setFunction(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("function"), tc.getFunction(), charset));
            tc.setUserAgent(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("userAgent"), tc.getUserAgent(), charset));
            tc.setScreenSize(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("screenSize"), tc.getScreenSize(), charset));
            tc.setHowTo(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("howTo"), tc.getHowTo(), charset));
            tc.setBehaviorOrValueExpected(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("behaviorOrValueExpected"), tc.getBehaviorOrValueExpected(), charset));

            // TODO verify, this setteer was not call on "create test case"
            tc.setConditionOper(ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("conditionOper"), tc.getConditionOper(), charset));
            // Parameter that we cannot secure as we need the html --> We DECODE them
            tc.setConditionVal1(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("conditionVal1"), tc.getConditionVal1(), charset));
            tc.setConditionVal2(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("conditionVal2"), tc.getConditionVal2(), charset));
            tc.setConditionVal3(ParameterParserUtil.parseStringParamAndDecode(request.getParameter("conditionVal3"), tc.getConditionVal3(), charset));

            return tc;
        } catch (UnsupportedOperationException e) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.GENERIC_ERROR), e);
        }
    }

}
