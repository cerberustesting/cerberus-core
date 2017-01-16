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
package org.cerberus.servlet.zzpublic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.util.ParamRequestMaker;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Executes the next test case contained into the execution queue.
 * 
 * <p>
 * No parameters needed.
 * </p>
 * 
 * @author abourdon
 */
@WebServlet(name = "ExecuteNextInQueue", urlPatterns = { "/ExecuteNextInQueue" })
public class ExecuteNextInQueue extends HttpServlet {

	/** The associated {@link Logger} to this class */
	private static final Logger LOG = Logger.getLogger(ExecuteNextInQueue.class);

	private static final long serialVersionUID = 1L;

	private ITestCaseExecutionInQueueService inQueueService;

	@Override
	public void init() throws ServletException {
		ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		inQueueService = appContext.getBean(ITestCaseExecutionInQueueService.class);
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
	 * <li>Getting the next test case to be executed;</li>
	 * <li>Execute it.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Part 1: Getting the next test case to be executed.
		List<TestCaseExecutionInQueue> inQueues = null;
		try {
			inQueues = inQueueService.toQueued(1);
		} catch (CerberusException ce) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ce.getMessage());
			return;
		}

		if (inQueues == null || inQueues.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No execution in queue");
			}
			return;
		}

		// Part 2: Execute it
		executeNext(inQueues.get(0), req, resp);
	}

	/**
	 * Constructs a {@link ParamRequestMaker} from the given
	 * {@link TestCaseExecutionInQueue}
	 * 
	 * @param lastInQueue
	 *            the {@link TestCaseExecutionInQueue} from which the
	 *            {@link ParamRequestMaker} is filled
	 * 
	 * @return a {@link ParamRequestMaker} from the given
	 *         {@link TestCaseExecutionInQueue}
	 */
	private static ParamRequestMaker makeParamRequestfromLastInQueue(TestCaseExecutionInQueue lastInQueue) {
		ParamRequestMaker paramRequestMaker = new ParamRequestMaker();
		paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST, lastInQueue.getTest());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST_CASE, lastInQueue.getTestCase());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_COUNTRY, lastInQueue.getCountry());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_ENVIRONMENT, lastInQueue.getEnvironment());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT, lastInQueue.getRobot());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_IP, lastInQueue.getRobotIP());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_PORT, lastInQueue.getRobotPort());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER, lastInQueue.getBrowser());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER_VERSION, lastInQueue.getBrowserVersion());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_PLATFORM, lastInQueue.getPlatform());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_URL, lastInQueue.isManualURL() ? ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE : null);
		if (lastInQueue.isManualURL()) {
			paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_HOST, lastInQueue.getManualHost());
			paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_CONTEXT_ROOT, lastInQueue.getManualContextRoot());
			paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_LOGIN_RELATIVE_URL, lastInQueue.getManualLoginRelativeURL());
			paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_ENV_DATA, lastInQueue.getManualEnvData());
		}
		paramRequestMaker.addParam(RunTestCase.PARAMETER_TAG, lastInQueue.getTag());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_OUTPUT_FORMAT, lastInQueue.getOutputFormat());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_SCREENSHOT, Integer.toString(lastInQueue.getScreenshot()));
		paramRequestMaker.addParam(RunTestCase.PARAMETER_VERBOSE, Integer.toString(lastInQueue.getVerbose()));
		paramRequestMaker.addParam(RunTestCase.PARAMETER_TIMEOUT, lastInQueue.getTimeout());
		paramRequestMaker.addParam(RunTestCase.PARAMETER_SYNCHRONEOUS, lastInQueue.isSynchroneous() ? ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE
				: ParameterParserUtil.DEFAULT_BOOLEAN_FALSE_VALUE);
		paramRequestMaker.addParam(RunTestCase.PARAMETER_PAGE_SOURCE, Integer.toString(lastInQueue.getPageSource()));
		paramRequestMaker.addParam(RunTestCase.PARAMETER_SELENIUM_LOG, Integer.toString(lastInQueue.getSeleniumLog()));
                paramRequestMaker.addParam(RunTestCase.PARAMETER_EXECUTION_QUEUE_ID, Long.toString(lastInQueue.getId()));
		return paramRequestMaker;
	}

	/**
	 * Executes the next test case represented by the given
	 * {@link TestCaseExecutionInQueue}
	 * 
	 * @param lastInQueue
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	private void executeNext(TestCaseExecutionInQueue lastInQueue, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String charset = resp.getCharacterEncoding();
		String query = "";
		try {
			ParamRequestMaker paramRequestMaker = makeParamRequestfromLastInQueue(lastInQueue);
			// TODO : Prefer use mkString(charset) instead of mkString().
			// However the RunTestCase servlet does not decode parameters,
			// then we have to mkString() without using charset
			query = paramRequestMaker.mkString();
		} catch (IllegalArgumentException iae) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iae.getMessage());
			return;
		} catch (IllegalStateException ise) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ise.getMessage());
			return;
		}

		CloseableHttpClient httpclient = null;
		HttpGet httpget = null;
		try {
			httpclient = HttpClients.createDefault();
			URI uri = new URIBuilder().setScheme(req.getScheme()).setHost(req.getServerName()).setPort(req.getServerPort()).setPath(req.getContextPath() + RunTestCase.SERVLET_URL)
					.setCustomQuery(query).build();
			httpget = new HttpGet(uri);
		} catch (URISyntaxException use) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, use.getMessage());
			return;
		}

		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
				resp.sendError(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
			}
		} catch (ClientProtocolException cpe) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, cpe.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}

	}
}
