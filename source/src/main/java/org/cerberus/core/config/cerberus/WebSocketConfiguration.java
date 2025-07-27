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
package org.cerberus.core.config.cerberus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.websocket.ChatWithAIWebSocket;
import org.cerberus.core.websocket.QueueStatusWebSocket;
import org.cerberus.core.websocket.ExecutionMonitorWebSocket;
import org.cerberus.core.websocket.TestCaseExecutionWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private static final Logger LOG = LogManager.getLogger(WebSocketConfiguration.class);

    @Autowired
    private TestCaseExecutionWebSocket testCaseExecutionWebSocket;
    @Autowired
    private ChatWithAIWebSocket chatWithAIWebSocket;
    @Autowired
    private QueueStatusWebSocket queueStatusWebSocket;
    @Autowired
    private ExecutionMonitorWebSocket executionMonitorWebSocket;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWithAIWebSocket, "/ws/chatai")
                .setAllowedOrigins("*");
        registry.addHandler(queueStatusWebSocket, "/ws/queuestatus")
                .setAllowedOrigins("*");
        registry.addHandler(executionMonitorWebSocket, "/ws/executionmonitor")
                .setAllowedOrigins("*");
        registry.addHandler(testCaseExecutionWebSocket, "/ws/execution/{execution-id}")
                .addInterceptors(new HttpHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

    /**
     Intercept to extract param from URL before handshake
     */
    private static class HttpHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest) {
                HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

                String uri = servletRequest.getRequestURI();  // e.g., "/ws/execution/12345"
                String executionId = uri.substring(uri.lastIndexOf('/') + 1);
                LOG.debug("Before Handshake : executionId:"+executionId);
                attributes.put("executionId", executionId);
            }
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {

        }
    }


}