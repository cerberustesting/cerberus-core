/* Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.engine.gwt.impl;

import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.exception.CerberusEventException;
import org.cerberus.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by corentin on 20/10/16.
 */
@Service
public class VariableService implements IVariableService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VariableService.class);

    @Autowired
    PropertyService propertyService;
    @Autowired
    ApplicationObjectVariableService applicationObjectVariableService;

    @Override
    public String decodeStringCompletly(String stringToDecode, TestCaseExecution testCaseExecution, TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {
        String result = stringToDecode;

        /**
         * Nothing to decode if null or empty string.
         */
        if (StringUtil.isNullOrEmpty(result)) {
            return result;
        }
        
        /**
         * Decode System Variables.
         */
        if (result.contains("%")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to decode (system variable) string : " + result);
            }
            result = propertyService.decodeStringWithSystemVariable(result, testCaseExecution);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (system variable). Result : " + result);
            }
        } else {
            return result;
        }

        /**
         * Decode ApplicationObject.
         */
        if (result.contains("%")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to decode (Application Object) string : " + result);
            }
            result = applicationObjectVariableService.decodeStringWithApplicationObject(result, testCaseExecution, forceCalculation);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (Application Object). Result : " + result);
            }
        } else {
            return result;
        }

        /**
         * Decode Properties.
         */
        if (result.contains("%")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to decode (Properties) string : " + result);
            }
            result = propertyService.decodeStringWithExistingProperties(result, testCaseExecution, testCaseStepActionExecution, forceCalculation);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (Properties). Result : " + result);
            }
        } else {
            return result;
        }

        return result;
    }
}
