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
package org.cerberus.service.authentification.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.service.authentification.IAPIKeyService;
import org.cerberus.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 * @author vertigo17
 *
 */
@Service
public class APIKeyService implements IAPIKeyService {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(APIKeyService.class);

    @Autowired
    private IParameterService parameterService;

    @Override
    public boolean checkAPIKey(HttpServletRequest request, HttpServletResponse response) {
        try {
            LOG.debug("Checking API Call.");

            boolean toSecure = parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_apikey_enable, "", false);

            String accessKey = request.getHeader("apikey");

            if (toSecure) {
                String message = "";
                String returnCode = "OK";
                if ((!StringUtil.isNullOrEmpty(accessKey)) && ((accessKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value1, "", "")))
                        || (accessKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value2, "", "")))
                        || (accessKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value3, "", "")))
                        || (accessKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value4, "", "")))
                        || (accessKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value5, "", ""))))) {
                    return true;
                } else {
                    JSONObject data = new JSONObject();
                    message = "Invalid API Key (please feed a valid apikey value inside HTTP Headers) !!";
                    returnCode = "KO";
                    data.put("message", message);
                    data.put("returnCode", returnCode);
                    response.getWriter().print(data.toString(1));
                    response.setStatus(401);
                    return false;
                }
            } else {
                return true;
            }
        } catch (JSONException ex) {
            LOG.error("JSON Exception when checking API Key.", ex);
        } catch (IOException ex) {
            LOG.error("IO Exception when checking API Key.", ex);
        }
        return false;

    }

}
