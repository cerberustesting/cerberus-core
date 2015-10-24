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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * {Insert class description here}
 *
 * @author FNogueira
 * @version 1.0, 24/10/2015
 * @since 1.1.3
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
      // create the thread pool
        ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 200, 50000L,TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
        sce.getServletContext().setAttribute("executor",executor);
        org.apache.log4j.Logger.getLogger(AppContextListener.class.getName()).log(org.apache.log4j.Level.WARN, "Context initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) sce.getServletContext().getAttribute("executor");
        executor.shutdown();
        org.apache.log4j.Logger.getLogger(AppContextListener.class.getName()).log(org.apache.log4j.Level.WARN, "Context Destroy");
    }
    
}
