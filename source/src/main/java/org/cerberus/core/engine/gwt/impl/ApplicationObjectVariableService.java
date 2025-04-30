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
package org.cerberus.core.engine.gwt.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.ApplicationObject;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.service.IApplicationObjectService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.gwt.IApplicationObjectVariableService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.util.answer.AnswerItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cerberus.core.util.StringUtil;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @since 0.9.0
 */
@Service
public class ApplicationObjectVariableService implements IApplicationObjectVariableService {

    private static final Logger LOG = LogManager.getLogger(ApplicationObjectVariableService.class);

    @Autowired
    private IApplicationObjectService applicationObjectService;
    @Autowired
    private IParameterService parameterService;

    /**
     * The property variable {@link Pattern}
     */
    public static final Pattern PROPERTY_VARIABLE_PATTERN = Pattern.compile("%object\\.[^%]+%");

    @Override
    public String decodeStringWithApplicationObject(String stringToDecode, TestCaseExecution tCExecution, boolean forceCalculation) throws CerberusEventException {
        String stringToDecodeInit = stringToDecode;
        String application = "";
        String system = "";

        if (tCExecution != null) {
            system = tCExecution.getApplicationObj().getSystem();
            application = tCExecution.getApplicationObj().getApplication();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting to decode string (application Object) : " + stringToDecode);
        }

        /**
         * Look at all the potencial properties still contained in
         * StringToDecode (considering that properties are between %).
         */
        List<String> internalAppObjectsFromStringToDecode = this.getApplicationObjectsStringListFromString(stringToDecode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Internal potencial application objects still found inside String '" + stringToDecode + "' : " + internalAppObjectsFromStringToDecode);
        }

        if (internalAppObjectsFromStringToDecode.isEmpty()) { // We escape if no property found on the string to decode
            if (LOG.isDebugEnabled()) {
                LOG.debug("Finished to decode (no application objects detected in string). Result : '" + stringToDecodeInit + "' to :'" + stringToDecode + "'");
            }
            return stringToDecode;
        }

        Iterator i = internalAppObjectsFromStringToDecode.iterator();
        while (i.hasNext()) {
            String value = (String) i.next();
            String[] valueA = value.split("\\.");
            if (valueA.length >= 3) {
                AnswerItem ans = applicationObjectService.readByKey(application, valueA[1]);
                if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && ans.getItem() != null) {
                    ApplicationObject ao = (ApplicationObject) ans.getItem();
                    String val = null;
                    if ("picturepath".equals(valueA[2])) {
                        val = parameterService.getParameterStringByKey("cerberus_applicationobject_path", "", "") + File.separator + ao.getID() + File.separator + ao.getScreenshotFilename();
                    } else if ("pictureurl".equals(valueA[2])) {
                        try {
                            val = parameterService.getParameterStringByKey("cerberus_url", system, "")
                                    + "/ReadApplicationObjectImage?application=" + StringUtil.encodeURL(ao.getApplication())
                                    + "&object=" + StringUtil.encodeURL(ao.getObject())
                                    + "#xoffset=" + ao.getXOffset() + "|yoffset=" + ao.getYOffset();
                        } catch (UnsupportedEncodingException ex) {
                            LOG.error("Error when encoding string in URL : ", ex);
                        }
                    } else if ("value".equals(valueA[2])) {
                        val = ao.getValue();
                    } else if ("base64".equals(valueA[2])) {
                        String filePath = parameterService.getParameterStringByKey("cerberus_applicationobject_path", "", "") + File.separator + ao.getID() + File.separator + ao.getScreenshotFilename();
                        try {
                            File file = new File(filePath);
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            val = Base64.getEncoder().encodeToString(fileContent);
                        } catch (IOException e) {
                            val = "!! DECODE ERROR - could not read file :'" + filePath + "' !!";
                            LOG.error(val, e);
                        }
                    }
                    if (val != null) {
                        stringToDecode = stringToDecode.replace("%" + value + "%", val);
                    }
                }
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
     * @param str the {@link String} to get all properties
     * @return a list of properties contained into the given {@link String}
     * @see #PROPERTY_VARIABLE_PATTERN
     */
    private List<String> getApplicationObjectsStringListFromString(String str) {
        List<String> properties = new ArrayList<>();
        if (str == null) {
            return properties;
        }

        Matcher propertyMatcher = PROPERTY_VARIABLE_PATTERN.matcher(str);
        while (propertyMatcher.find()) {
            String rawProperty = propertyMatcher.group();
            // Removes the first and last '%' character to only get the property name
            rawProperty = rawProperty.substring(1, rawProperty.length() - 1);
            properties.add(rawProperty);
        }
        return properties;
    }

}
