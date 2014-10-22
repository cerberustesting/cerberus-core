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
package org.cerberus.servlet.publi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cerberus.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.exception.FactoryCreationException;
import org.cerberus.factory.IFactoryTestCaseExecutionInQueue;
import org.cerberus.service.ITestCaseExecutionInQueueService;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Add a test case to the execution queue (so to be executed later).
 * 
 * @author abourdon
 */
@WebServlet(name = "AddToExecutionQueue", urlPatterns = { "/AddToExecutionQueue" })
public class AddToExecutionQueue extends HttpServlet {

	/**
	 * Exception thrown when the parameter scanning process goes wrong.
	 * 
	 * @author abourdon
	 */
	private static class ParameterException extends Exception {

		private static final long serialVersionUID = 1L;

		public ParameterException(String message) {
			super(message);
		}

		public ParameterException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(AddToExecutionQueue.class);

	private static final String PARAMETER_SELECTED_TEST = "SelectedTest";
	private static final String PARAMETER_SELECTED_TEST_TEST = "Test";
	private static final String PARAMETER_SELECTED_TEST_TEST_CASE = "TestCase";
	private static final String PARAMETER_COUNTRY = "Country";
	private static final String PARAMETER_ENVIRONMENT = "Environment";
	private static final String PARAMETER_ROBOT = "Robot";
	private static final String PARAMETER_ROBOT_IP = "RobotIP";
	private static final String PARAMETER_ROBOT_PORT = "RobotPort";
	private static final String PARAMETER_BROWSER = "Browser";
	private static final String PARAMETER_BROWSER_VERSION = "BrowserVersion";
	private static final String PARAMETER_PLATFORM = "Platform";
	private static final String PARAMETER_MANUAL_URL = "ManualURL";
	private static final String PARAMETER_MANUAL_HOST = "ManualHost";
	private static final String PARAMETER_MANUAL_CONTEXT_ROOT = "ManualContextRoot";
	private static final String PARAMETER_MANUAL_LOGIN_RELATIVE_URL = "ManualLoginRelativeURL";
	private static final String PARAMETER_MANUAL_ENV_DATA = "ManualEnvData";
	private static final String PARAMETER_TAG = "Tag";
	private static final String PARAMETER_OUTPUT_FORMAT = "OutputFormat";
	private static final String PARAMETER_SCREENSHOT = "Screenshot";
	private static final String PARAMETER_VERBOSE = "Verbose";
	private static final String PARAMETER_TIMEOUT = "Timeout";
	private static final String PARAMETER_SYNCHRONEOUS = "Synchroneous";
	private static final String PARAMETER_PAGE_SOURCE = "PageSource";
	private static final String PARAMETER_SELENIUM_LOG = "SeleniumLog";

	private static final String DEFAULT_VALUE_OUTPUT_FORMAT = "gui";
	private static final int DEFAULT_VALUE_SCREENSHOT = 0;
	private static final boolean DEFAULT_VALUE_MANUAL_URL = false;
	private static final int DEFAULT_VALUE_VERBOSE = 0;
	private static final long DEFAULT_VALUE_TIMEOUT = 15000;
	private static final boolean DEFAULT_VALUE_SYNCHRONEOUS = false;
	private static final int DEFAULT_VALUE_PAGE_SOURCE = 1;
	private static final int DEFAULT_VALUE_SELENIUM_LOG = 1;

	private static final String LINE_SEPARATOR = "\n";

	private ITestCaseExecutionInQueueService inQueueService;
	private IFactoryTestCaseExecutionInQueue inQueueFactoryService;

	@Override
	public void init() throws ServletException {
		ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		inQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);
		inQueueFactoryService = appContext.getBean(IFactoryTestCaseExecutionInQueue.class);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	/**
	 * Process request for both GET and POST method.
	 * 
	 * <p>
	 * Request processing is divided in two parts:
	 * <ol>
	 * <li>Getting all test cases which have been sent to this servlet;</li>
	 * <li>Try to insert all these test cases to the execution queue.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Part 1: Getting all test cases which have been sent to this servlet.
		List<TestCaseExecutionInQueue> toInserts = null;
		try {
			toInserts = getTestCasesToInsert(req);
		} catch (ParameterException pe) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, pe.getMessage());
			return;
		}

		// Part 2: Try to insert all these test cases to the execution queue.
		List<String> errorMessages = new ArrayList<String>();
		for (TestCaseExecutionInQueue toInsert : toInserts) {
			try {
				if (inQueueService.canInsert(toInsert)) {
					inQueueService.insert(toInsert);
				}
			} catch (CerberusException e) {
				String errorMessage = "Unable to insert " + toInsert.toString() + " due to " + e.getMessage();
				LOG.warn(errorMessage);
				errorMessages.add(errorMessage);
				continue;
			}
		}

		if (!errorMessages.isEmpty()) {
			StringBuilder errorMessage = new StringBuilder();
			for (String item : errorMessages) {
				errorMessage.append(item);
				errorMessage.append(LINE_SEPARATOR);
			}
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage.toString());
		}
                
                resp.sendRedirect("ReportingExecutionByTag.jsp?Tag="+req.getParameter(PARAMETER_TAG));

	}

	/**
	 * Gets all test cases requested to be inserted into the execution queue
	 * 
	 * @param req
	 * @return a {@link List} of {@link TestCaseExecutionInQueue} which have
	 *         been defined into the request.
	 * @throws ParameterException
	 */
	private List<TestCaseExecutionInQueue> getTestCasesToInsert(HttpServletRequest req) throws ParameterException {
		String charset = req.getCharacterEncoding();

		List<Map<String, String>> selectedTests = ParameterParserUtil.parseListMapParamAndDecode(req.getParameterValues(PARAMETER_SELECTED_TEST), null, charset);
		if (selectedTests == null || selectedTests.isEmpty()) {
			throw new ParameterException("Selected test must not be null");
		}
		List<String> countries = ParameterParserUtil.parseListParamAndDecode(req.getParameterValues(PARAMETER_COUNTRY), null, charset);
		if (countries == null || countries.isEmpty()) {
			throw new ParameterException("Countries can not be null");
		}
		String environment = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_ENVIRONMENT), null, charset);
		if (environment == null || environment.isEmpty()) {
			throw new ParameterException("Environment must not be null");
		}
		String browser = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_BROWSER), null, charset);
		if (browser == null || browser.isEmpty()) {
			throw new ParameterException("Browser must not be null");
		}
		String tag = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_TAG), null, charset);
		if (tag == null || tag.isEmpty()) {
			throw new ParameterException("Tag must not be null");
		}
		Date requestDate = new Date();

		String robot = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_ROBOT), null, charset);
		String robotIP = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_ROBOT_IP), null, charset);
		String robotPort = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_ROBOT_PORT), null, charset);
		String browserVersion = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_BROWSER_VERSION), null, charset);
		String platform = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_PLATFORM), null, charset);
		boolean manualURL = ParameterParserUtil.parseBooleanParamAndDecode(req.getParameter(PARAMETER_MANUAL_URL), DEFAULT_VALUE_MANUAL_URL, charset);
		String manualHost = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_MANUAL_HOST), null, charset);
		String manualContextRoot = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_MANUAL_CONTEXT_ROOT), null, charset);
		String manualLoginRelativeURL = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_MANUAL_LOGIN_RELATIVE_URL), null, charset);
		String manualEnvData = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_MANUAL_ENV_DATA), null, charset);
		String outputFormat = ParameterParserUtil.parseStringParamAndDecode(req.getParameter(PARAMETER_OUTPUT_FORMAT), DEFAULT_VALUE_OUTPUT_FORMAT, charset);
		int screenshot = ParameterParserUtil.parseIntegerParamAndDecode(req.getParameter(PARAMETER_SCREENSHOT), DEFAULT_VALUE_SCREENSHOT, charset);
		int verbose = ParameterParserUtil.parseIntegerParamAndDecode(req.getParameter(PARAMETER_VERBOSE), DEFAULT_VALUE_VERBOSE, charset);
		long timeout = ParameterParserUtil.parseLongParamAndDecode(req.getParameter(PARAMETER_TIMEOUT), DEFAULT_VALUE_TIMEOUT, charset);
		boolean synchroneous = ParameterParserUtil.parseBooleanParamAndDecode(req.getParameter(PARAMETER_SYNCHRONEOUS), DEFAULT_VALUE_SYNCHRONEOUS, charset);
		int pageSource = ParameterParserUtil.parseIntegerParamAndDecode(req.getParameter(PARAMETER_PAGE_SOURCE), DEFAULT_VALUE_PAGE_SOURCE, charset);
		int seleniumLog = ParameterParserUtil.parseIntegerParamAndDecode(req.getParameter(PARAMETER_SELENIUM_LOG), DEFAULT_VALUE_SELENIUM_LOG, charset);

		List<TestCaseExecutionInQueue> inQueues = new ArrayList<TestCaseExecutionInQueue>();
		for (Map<String, String> selectedTest : selectedTests) {
			String test = selectedTest.get(PARAMETER_SELECTED_TEST_TEST);
			String testCase = selectedTest.get(PARAMETER_SELECTED_TEST_TEST_CASE);
			for (String country : countries) {
				try {
					inQueues.add(inQueueFactoryService.create(test,
							testCase,
							country,
							environment,
							robot,
							robotIP,
							robotPort,
							browser,
							browserVersion,
							platform,
							manualURL,
							manualHost,
							manualContextRoot,
							manualLoginRelativeURL,
							manualEnvData,
							tag,
							outputFormat,
							screenshot,
							verbose,
							timeout,
							synchroneous,
							pageSource,
							seleniumLog,
							requestDate));
				} catch (FactoryCreationException e) {
					throw new ParameterException("Unable to insert record due to: " + e.getMessage(), e);
				}
			}
		}
		return inQueues;
	}
}
