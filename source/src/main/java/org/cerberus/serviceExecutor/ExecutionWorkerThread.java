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
package org.cerberus.serviceExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.entity.ExecutionThreadPool;

/**
 *
 * @author bcivel
 */
public class ExecutionWorkerThread implements Runnable, Comparable {

    private String command;
    private ExecutionThreadPool execThreadPool;

    public void setCommand(String s) {
        this.command = s;
    }

    public void setExecThreadPool(ExecutionThreadPool etp) {
        this.execThreadPool = etp;
    }

    @Override
    public void run() {

        try {
            processCommand(command);
            execThreadPool.decrementSize();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ExecutionWorkerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExecutionWorkerThread.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(ExecutionWorkerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processCommand(String url) throws MalformedURLException, IOException {
        URL urlToCall = new URL(url);
        execThreadPool.incrementInExecution();
        HttpURLConnection c = null;
        BufferedReader br = null;
        try {
            c = (HttpURLConnection) urlToCall.openConnection();

            // set the connection timeout to 600 seconds
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
            System.out.print("TimeOut Exception " + ex);
        } finally {
            if (null != br) {
                br.close();
            }
            if (null != c) {
                c.disconnect();
            }
        }

        execThreadPool.decrementInExecution();
    }

    @Override
    public String toString() {
        return this.command;
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
