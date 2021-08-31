/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.service.groovy.impl;

import groovy.lang.GroovyShell;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.cerberus.service.groovy.IGroovyService;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
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
                        Collections.<Class<?>>emptySet(),
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

}
