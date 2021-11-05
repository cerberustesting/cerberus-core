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
import java.security.Principal;
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
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        try {
            LOG.debug("Checking API Call.");

            if (this.isApiKeyAuthEnabled()) {

                // If already aauthorised, we don't need to check the api key.
                LOG.debug(request.getUserPrincipal());
                if ((request.getUserPrincipal() != null) && (!StringUtil.isNullOrEmpty(request.getUserPrincipal().getName()))) {
                    LOG.debug("User connected with : '" + request.getUserPrincipal().getName() + "'");
                    return true;
                }

                String apiKey = request.getHeader("apikey");

                if (isApiKeyValid(apiKey)) {
                    return true;
                } else {
                    JSONObject data = new JSONObject();
                    data.put("message", "Invalid API Key (please feed a valid apikey value inside HTTP Headers) !!");
                    data.put("returnCode", "KO");
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

    @Override
    public boolean authenticate(String apiKey) {
        return isApiKeyAuthEnabled() && isApiKeyValid(apiKey);
    }

    @Override
    public boolean authenticate(Principal principal, String apiKey) {
        return (principal != null && !StringUtil.isNullOrEmpty(principal.getName())) || this.authenticate(apiKey);
    }

    public boolean isApiKeyAuthEnabled() {
        return parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_apikey_enable, "", true);
    }

    private boolean isApiKeyValid(String apiKey) {
        return (!StringUtil.isNullOrEmpty(apiKey))
                && ((apiKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value1, "", "")))
                || (apiKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value2, "", "")))
                || (apiKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value3, "", "")))
                || (apiKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value4, "", "")))
                || (apiKey.equals(parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_apikey_value5, "", ""))));
    }

}
