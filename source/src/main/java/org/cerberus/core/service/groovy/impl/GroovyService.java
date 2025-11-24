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
package org.cerberus.core.service.groovy.impl;

import groovy.lang.GroovyShell;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.config.cerberus.Property;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.impl.ParameterService;
import org.cerberus.core.service.groovy.IGroovyService;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {@link IGroovyService} default implementation
 *
 * @author Aurelien Bourdon
 */
@Service
public class GroovyService implements IGroovyService {

    /**
     * Groovy specific compilation customizer in order to avoid code injection
     */
    private static final CompilerConfiguration GROOVY_COMPILER_CONFIGURATION = new CompilerConfiguration().addCompilationCustomizers(new SandboxTransformer());

    @Autowired
    private ParameterService parameterService;

    private static final Logger LOG = LogManager.getLogger(GroovyService.class);

    /**
     * Each Groovy execution is ran inside a dedicated {@link Thread},
     * especially to register our Groovy interceptor
     */
    private ExecutorService executorService;

    @PostConstruct
    private void init() {
        executorService = Executors.newCachedThreadPool();
    }

    @PreDestroy
    private void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    @Override
    public String eval(final String script) throws IGroovyServiceException {
        try {
            Future<String> expression = executorService.submit(() -> {
                RestrictiveGroovyInterceptor interceptor = new RestrictiveGroovyInterceptor(
                        Property.isSaaS() ? Collections.emptySet() : getAdditionalClasses(),
                        Collections.<Class<?>>emptySet(),
                        Collections.<RestrictiveGroovyInterceptor.AllowedPrefix>emptyList()
                );
                try {
                    interceptor.register();
                    GroovyShell shell = new GroovyShell(GROOVY_COMPILER_CONFIGURATION);
                    return shell.evaluate(script).toString();
                } finally {
                    interceptor.unregister();
                }
            });

            String eval = expression.get();
            if (eval == null) {
                throw new IGroovyServiceException("Groovy evaluation returns null result");
            }
            return eval;
        } catch (Exception e) {
            throw new IGroovyServiceException(e);
        }
    }

    private Set<Class<?>> getAdditionalClasses() {
        Set<Class<?>> additionalClasses = new HashSet<>();
        String[] extraClasses = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_groovy_classes_whitelist, "", "")
                .split(",");

        for (String extraClass : extraClasses) {
            try {
                additionalClasses.add(
                        Class.forName(extraClass.trim())
                );
            } catch (ClassNotFoundException exception) {
                LOG.warn(String.format("Extra class '%s' to add in the Groovy whitelist was not found.", extraClass));
            }
        }

        return additionalClasses;
    }

}
