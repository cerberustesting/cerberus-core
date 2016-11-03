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
package org.cerberus.websocket;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.ITestCaseExecutionService;
import org.cerberus.crud.service.impl.TestCaseExecutionService;
import org.cerberus.crud.service.impl.TestCaseStepExecutionService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.websocket.config.ServletAwareConfig;
import org.cerberus.websocket.decoders.TestCaseExecutionDecoder;
import org.cerberus.websocket.encoders.TestCaseExecutionEncoder;
import org.cerberus.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

    @ServerEndpoint(
            value = "/execution/{execution-id}",
            decoders = {TestCaseExecutionDecoder.class},
            encoders = {TestCaseExecutionEncoder.class},
            configurator = ServletAwareConfig.class
    )
    public class TestCaseExecutionEndPoint {
        private static final Logger LOG = Logger.getLogger(TestCaseExecutionEndPoint.class);

        private ApplicationContext appContext;

        /**
         * All open WebSocket sessions
         */
        static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

        /**
         * Send Live message for all peers connected to this execution
         *
         * @param msg
         */
        public static void send(TestCaseExecution msg) {
            try {
            /* Send updates to all open WebSocket sessions for this match */
                for (Session session : peers) {
                    if (Boolean.TRUE.equals(session.getUserProperties().get(String.valueOf(msg.getId())))) {
                        if (session.isOpen()) {
                            session.getBasicRemote().sendObject(msg);
                        }
                    }
                }
            } catch (IOException | EncodeException e) {

            }
        }

        @OnMessage
        public void message(final Session session, TestCaseExecution msg, @PathParam("execution-id") int executionId) {
        }

        @OnOpen
        public void openConnection(Session session, EndpointConfig config, @PathParam("execution-id") int executionId) {

            session.getUserProperties().put(String.valueOf(executionId), true);
            peers.add(session);


            HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
            ServletContext servletContext = httpSession.getServletContext();
            appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

            ITestCaseExecutionService testCaseExecutionService = appContext.getBean(TestCaseExecutionService.class);

            AnswerItem ans = testCaseExecutionService.readByKey(executionId);

            if(ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && ans.getItem() != null){
                TestCaseExecution tce = (TestCaseExecution) ans.getItem();
                testCaseExecutionService.sendObjectByWebSocket(tce);
            }

        }

        @OnClose
        public void closedConnection(Session session, @PathParam("execution-id") int executionId) {
            session.getUserProperties().put(String.valueOf(executionId), false);
            peers.remove(session);
        }

        @OnError
        public void error(Session session, Throwable t) {

        }
    }

