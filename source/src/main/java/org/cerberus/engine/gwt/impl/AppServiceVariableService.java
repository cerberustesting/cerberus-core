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
package org.cerberus.engine.gwt.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.engine.gwt.IAppServiceVariableService;
import org.cerberus.exception.CerberusEventException;
import org.springframework.stereotype.Service;

/**
 * @author Ryltar
 */
@Service
public class AppServiceVariableService implements IAppServiceVariableService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AppServiceVariableService.class);

    /**
     * The property variable {@link Pattern}
     */
    public static final Pattern PROPERTY_VARIABLE_PATTERN = Pattern.compile("%service\\.[^%]+%");

    @Override
    public String decodeStringWithAppService(String stringToDecode, TestCaseExecution tCExecution, boolean forceCalculation) throws CerberusEventException {


        String stringToDecodeInit = stringToDecode;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to decode string (application Service) : " + stringToDecode);
        }

        /**
         * Look at all the potencial properties still contained in
         * StringToDecode (considering that properties are between %).
         */
        List<String> internalAppServicesFromStringToDecode = this.getApplicationServiceStringListFromString(stringToDecode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Internal potencial application service still found inside String '" + stringToDecode + "' : " + internalAppServicesFromStringToDecode);
        }

        if (internalAppServicesFromStringToDecode.isEmpty()) { // We escape if no property found on the string to decode
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (no application objects detected in string). Result : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
            }
            return stringToDecode;
        }

        Iterator i = internalAppServicesFromStringToDecode.iterator();
        while (i.hasNext()) {
            String value = (String) i.next();
            String[] valueA = value.split("\\.");
            String val = null;
            val = valueA[1];

            if (val != null) {
                stringToDecode = stringToDecode.replace("%" + value + "%", val);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished to decode String (application Object) : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
        }
        return stringToDecode;
    }

    /**
     * Gets all properties names contained into the given {@link String}
     *
     * <p>
     * A property is defined by including its name between two '%' character.
     * </p>
     *
     * @see #PROPERTY_VARIABLE_PATTERN
     * @param str the {@link String} to get all properties
     * @return a list of properties contained into the given {@link String}
     */
    private List<String> getApplicationServiceStringListFromString(String str) {
        List<String> services = new ArrayList<String>();
        if (str == null) {
            return services;
        }

        Matcher propertyMatcher = PROPERTY_VARIABLE_PATTERN.matcher(str);
        while (propertyMatcher.find()) {
            String rawProperty = propertyMatcher.group();
            // Removes the first and last '%' character to only get the property name
            rawProperty = rawProperty.substring(1, rawProperty.length() - 1);
            services.add(rawProperty);
        }
        return services;
    }

}
