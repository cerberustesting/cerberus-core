/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.servlet.crud.testexecution;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.Tag;
import org.cerberus.core.crud.service.ITagService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.answer.Answer;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "UpdateTag", urlPatterns = {"/UpdateTag"})
public class UpdateTag extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(UpdateTag.class);
    private ITagService tagService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf8");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        tagService = appContext.getBean(ITagService.class);

        try {
            String tag = ParameterParserUtil.parseStringParam(request.getParameter("tag"), "");
            String desc = ParameterParserUtil.parseStringParam(request.getParameter("description"), null);
            String comment = ParameterParserUtil.parseStringParam(request.getParameter("comment"), null);
            Answer answer = new Answer();

            Tag tagObj = tagService.convert(tagService.readByKey(tag));

            if (desc != null) {
                tagObj.setDescription(desc);
                tagObj.setUsrModif(request.getUserPrincipal().getName());
                answer = tagService.updateDescription(tag, tagObj);
                jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", answer.getResultMessage().getDescription());
            }
            if (comment != null) {
                tagObj.setComment(comment);
                tagObj.setUsrModif(request.getUserPrincipal().getName());
                answer = tagService.updateComment(tag, tagObj);
                jsonResponse.put("messageType", answer.getResultMessage().getMessage().getCodeString());
                jsonResponse.put("message", answer.getResultMessage().getDescription());
            }

            response.getWriter().print(jsonResponse.toString());

        } catch (CerberusException | JSONException ex) {
            LOG.warn(ex, ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        processRequest(request, response);
    }

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
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
