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
package org.cerberus.service.groovy.impl;

import groovy.lang.GroovyRuntimeException;
import groovy.util.Eval;
import org.cerberus.service.groovy.IGroovyService;
import org.springframework.stereotype.Service;

/**
 * {@link IGroovyService} default implementation
 *
 * @author Aurelien Bourdon
 */
@Service
public class GroovyService implements IGroovyService {

    @Override
    public String eval(String script) throws IGroovyServiceException {
        try {
            Object eval = Eval.me(script);
            if (eval == null) {
                throw new IGroovyServiceException("Groovy evaluation returns null result");
            }
            return eval.toString();
        } catch (GroovyRuntimeException e) {
            throw new IGroovyServiceException(e);
        }
    }

}
