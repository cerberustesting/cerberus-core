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
package org.cerberus.servlet.zzpublic.async;

import javax.servlet.AsyncContext;

/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
public class AsyncRequestProcessor implements Runnable {

    private AsyncContext asyncContext;
    private int secs;
    
    @Override
    public void run() {
        String msg = "Async Supported? " + asyncContext.getRequest().isAsyncSupported();
        System.out.println(msg);
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, msg);
        longProcessing(secs);
        /*try {
                PrintWriter out = asyncContext.getResponse().getWriter();
                out.write("Processing done for " + secs + " milliseconds!!");
        } catch (IOException e) {
                e.printStackTrace();
        }*/
        //complete the processing
        asyncContext.complete();
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, "Process complete");
        
        /*try {

            //PrintWriter out = asyncContext.getResponse().getWriter();

            //out.write("Processing done for " + secs + " milliseconds!!");

        } catch (IOException e) {

            e.printStackTrace();

        }*/

	        //complete the processing
    }
    public AsyncRequestProcessor() {
        
    }

    public AsyncRequestProcessor(AsyncContext asyncCtx, int secs) {
        this.asyncContext = asyncCtx;
        this.secs = secs;
    }
    
    // TODO:FN remove test debug
    private void longProcessing(int secs) {
        // wait for given time before finishing
        //try {
            org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, "Processing..");
            //Thread.sleep(secs);

        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
    }
    
}
