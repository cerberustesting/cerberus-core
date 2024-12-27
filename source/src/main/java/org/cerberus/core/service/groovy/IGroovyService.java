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
package org.cerberus.core.service.groovy;

/**
 * Entry point to deal with the Groovy language
 *
 * @author Aurelien Bourdon
 */
public interface IGroovyService {

    /**
     * The associated exception class to any {@link IGroovyService}
     */
    class IGroovyServiceException extends Exception {

        public IGroovyServiceException(String message) {
            super(message);
        }

        public IGroovyServiceException(Throwable cause) {
            super(cause);
        }
        
    }

    /**
     * Evaluate the given Groovy script
     *
     * @param script the Groovy script to evaluate
     * @return the {@link String} result of the given Groovy script evaluation
     * @throws IGroovyServiceException if an error occurred during Groovy script
     * evaluation
     */
    String eval(String script) throws IGroovyServiceException;

}
