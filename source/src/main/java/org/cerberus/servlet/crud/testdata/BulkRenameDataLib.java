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
package org.cerberus.servlet.crud.testdata;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebServlet(name = "BulkRenameDataLib", urlPatterns = {"/BulkRenameDataLib"})
public class BulkRenameDataLib extends HttpServlet {

	private static final Logger LOG = LogManager.getLogger(CreateTestDataLib.class);

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

		ITestDataLibService tdls = appContext.getBean(ITestDataLibService.class);

		JSONObject jsonResponse = new JSONObject();
		response.setContentType("application/json");

		Answer ans = new Answer();

		/**
		 * Parsing and securing all required parameters.
		 */
		try {
			if (request.getParameter("oldname") != null || !request.getParameter("newname").isEmpty()) {
				// if any element is null, an error message is displayed and no operation is performed
				MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
				ans.setResultMessage(msg);
			}
			else {
				//tdls.bulkRename(request.getParameter("oldname"), request.getParameter("newname"));
				MessageEvent msg = new MessageEvent(MessageEventEnum.GENERIC_OK);
				ans.setResultMessage(msg);
			}

		} catch (NumberFormatException ex) {
			LOG.warn(ex);
		} finally {
			try {
				//sets the message returned by the operations
				jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
				jsonResponse.put("message", ans.getResultMessage().getDescription());
				response.getWriter().print(jsonResponse);
				response.getWriter().flush();
			} catch (JSONException ex) {
				LOG.warn(ex);
				response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
				response.getWriter().flush();
			}
		} 
	}
	
	 @Override
	    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
	        processRequest(request, response);
	    }
}
