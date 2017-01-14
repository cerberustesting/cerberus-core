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
package org.cerberus.engine.threadpool;

import org.apache.log4j.Logger;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.service.impl.TestCaseExecutionInQueueService;
import org.cerberus.engine.entity.ExecutionThreadPool;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * @author bcivel
 */
public class ExecutionWorkerThread implements Runnable, Comparable {

    private String executionUrl;
    private String executionInQueueId;

    private static final Logger LOG = Logger.getLogger(ExecutionWorkerThread.class);

    public ExecutionWorkerThread() {

    }

    public ExecutionWorkerThread(String executionUrlUrl, String executionInQueueId) {
        this.executionUrl = executionUrlUrl;
        this.executionInQueueId = executionInQueueId;
    }

    @Override
    public void run() {
        try {
            processCommand(executionUrl);
        } catch (Exception ex) {
            LOG.warn("Unable to start execution in queue " + executionInQueueId + " due to " + ex.toString(), ex);
        }
    }

    private void processCommand(String url) throws IOException {
        URL urlToCall = new URL(url);
        HttpURLConnection c = null;
        BufferedReader br = null;
        try {
            c = (HttpURLConnection) urlToCall.openConnection();
            c.setConnectTimeout(600000);
            c.setReadTimeout(600000);

            // get a stream to read data from url
            String str = "";
            StringBuilder sb = new StringBuilder();

            br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            while (null != (str = br.readLine())) {
                sb.append(str);
            }

        } catch (SocketTimeoutException ex) {
            LOG.warn("Unable to process execution in queue " + executionInQueueId + " due to " + ex.toString(), ex);
        } finally {
            if (null != br) {
                br.close();
            }
            if (null != c) {
                c.disconnect();
            }
        }
    }

    @Override
    public String toString() {
        return this.executionUrl;
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
