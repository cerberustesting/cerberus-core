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
package org.cerberus.servlet.documentation;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IDocumentationService;
import org.cerberus.util.ParameterParserUtil;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 * @version 1.0, 07/02/2013
 * @since 0.9.0
 */
@WebServlet(name = "DocumentationField", urlPatterns = {"/DocumentationField"})
public class DocumentationField extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String result = "";

        String docTable = policy.sanitize(httpServletRequest.getParameter("docTable"));
        String docField = policy.sanitize(httpServletRequest.getParameter("docField"));
        String docLabel = policy.sanitize(httpServletRequest.getParameter("docLabel"));
        String lang = ParameterParserUtil.parseStringParam(policy.sanitize(httpServletRequest.getParameter("lang")), "en");

        result = docService.findLabelHTML(docTable, docField, docLabel, lang);

        try {
            httpServletResponse.setContentType("text/html");
            httpServletResponse.getWriter().print(result);
        } catch (Exception exception) {
            MyLogger.log(DocumentationField.class.getName(), Level.WARN, exception.toString());
        }
    }
}
