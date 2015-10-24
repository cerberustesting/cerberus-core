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

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;


/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
public class AppAsyncListener implements AsyncListener{

    @Override
    public void onComplete(AsyncEvent ae) throws IOException {
        System.out.println("AppAsyncListener onComplete");
        org.apache.log4j.Logger.getLogger(AppAsyncListener.class.getName()).log(org.apache.log4j.Level.WARN, "AppAsyncListener onComplete");
        ServletResponse response = ae.getAsyncContext().getResponse();  
        PrintWriter out = response.getWriter(); 
        out.write("Processing completed");
    }

    @Override
    public void onTimeout(AsyncEvent ae) throws IOException {
        System.out.println("AppAsyncListener onTimeout");
	org.apache.log4j.Logger.getLogger(AppAsyncListener.class.getName()).log(org.apache.log4j.Level.WARN, "AppAsyncListener onTimeout");
        //we can send appropriate response to client
        ServletResponse response = ae.getAsyncContext().getResponse();  
        PrintWriter out = response.getWriter(); 
        out.write("TimeOut Error in Processing");
    }

    @Override
    public void onError(AsyncEvent ae) throws IOException {
        System.out.println("AppAsyncListener onError");
        org.apache.log4j.Logger.getLogger(AppAsyncListener.class.getName()).log(org.apache.log4j.Level.WARN, "AppAsyncListener onError");
        ServletResponse response = ae.getAsyncContext().getResponse();  
        PrintWriter out = response.getWriter(); 
        out.write("Error in Processing");
    }

    @Override
    public void onStartAsync(AsyncEvent ae) throws IOException {
        System.out.println("AppAsyncListener onStartAsync");
        org.apache.log4j.Logger.getLogger(AppAsyncListener.class.getName()).log(org.apache.log4j.Level.WARN, "AppAsyncListener onStartAsync");        
    }
    
}
