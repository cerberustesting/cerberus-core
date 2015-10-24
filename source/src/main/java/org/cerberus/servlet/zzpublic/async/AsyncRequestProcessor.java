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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.exception.CerberusException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
public class AsyncRequestProcessor implements Runnable {

    private AsyncContext asyncContext;
    private String test;
    private String testCase;
    private String result;
    
    @Override
    public void run() {
        String msg = "Async Supported? " + asyncContext.getRequest().isAsyncSupported();
        System.out.println(msg);
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, msg);
        processing();
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

    public AsyncRequestProcessor(AsyncContext asyncCtx, String test, String testCase) {
        this.asyncContext = asyncCtx;
        this.test = test;
        this.testCase = testCase;
    }
    
    // TODO:FN remove test debug
    private void processing() {
        
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(asyncContext.getRequest().getServletContext());
        ITestCaseService service = appContext.getBean(ITestCaseService.class);
        try {
            TCase tc = service.findTestCaseByKey(test, testCase);
            result = "KO " + tc.getTest() + " - " + tc.getTestCase() + " - " + tc.getDescription();
        } catch (CerberusException ex) {
            Logger.getLogger(AsyncRequestProcessor.class.getName()).log(Level.SEVERE, null, ex);
            result = "KO " + ex.toString();
        }
        org.apache.log4j.Logger.getLogger(AsyncRequestProcessor.class.getName()).log(org.apache.log4j.Level.WARN, "Processing..");
        
    }
    
}
