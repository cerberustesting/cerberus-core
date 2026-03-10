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

import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.cerberus.core.config.webmvc.WebMvcConfiguration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.EnumSet;

/**
 * Detected and called by Tomcat at startup via ServletContainerInitializer.
 */
public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        // Single shared Spring context for both root beans and MVC beans.
        // Avoids the classic root/servlet context split that causes issues with Spring Security.
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(CerberusConfiguration.class, WebMvcConfiguration.class);

        // ── ContextLoaderListener with shared context
        servletContext.addListener(new ContextLoaderListener(context));

        // ── Listeners
        servletContext.addListener(new RequestContextListener());

        // ── Context params
        servletContext.setInitParameter("THUMBNAIL", "Thumbnailer?p=");
        servletContext.setInitParameter("SHARED_DOCS", "Shared docs");

        // DispatcherServlet handles all /api/* for controllers via Spring MVC.
        // Uses the same shared context to allow Spring Security to access MVC beans.
        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic springrest = servletContext.addServlet("springrest", dispatcherServlet);
        springrest.setLoadOnStartup(1);
        springrest.addMapping("/api/*");

        // Multipart upload limits: 15MB per file, 30MB per request, no memory threshold
        springrest.setMultipartConfig(new MultipartConfigElement("/tmp",15_728_640L,31_457_280L,0));


        /**
         * Add Servlet Streamable for MCP
         */
        Object mcpProvider = HttpServletStreamableServerTransportProvider.builder()
                    .mcpEndpoint("/mcp")
                    .build();

        servletContext.setAttribute("mcpTransportProvider", mcpProvider);
        ServletRegistration.Dynamic mcpServlet = servletContext.addServlet("mcpServlet", (Servlet) mcpProvider);
        mcpServlet.setLoadOnStartup(2);
        mcpServlet.addMapping("/mcp");
        mcpServlet.setAsyncSupported(true);


        // Session expires after 600 minutes of inactivity
        servletContext.addListener(new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {se.getSession().setMaxInactiveInterval(600 * 60);}
        });

        // Registers Spring Security filter chain on all URLs and dispatcher types.
        // DelegatingFilterProxy delegates to the "springSecurityFilterChain" bean at runtime.
        FilterRegistration.Dynamic securityFilter = servletContext.addFilter("springSecurityFilterChain",new DelegatingFilterProxy("springSecurityFilterChain"));
        securityFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class),false,"/*");
        securityFilter.setAsyncSupported(true);

        // ── MySQL JDBC cleanup on shutdown
        servletContext.addListener(new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent sce) {
                // nothing
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                // Shutdown MySQL abandoned connection cleanup thread
                try {
                    com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
                } catch (Exception e) {
                    // ignore
                }
                // Deregister JDBC drivers registered by this classloader
                java.util.Enumeration<java.sql.Driver> drivers = java.sql.DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    try {
                        java.sql.DriverManager.deregisterDriver(drivers.nextElement());
                    } catch (java.sql.SQLException e) {
                        // ignore
                    }
                }
            }
        });
    }

}
