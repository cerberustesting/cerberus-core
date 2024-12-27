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
package org.cerberus.core.service.authentification.impl;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.authentification.IAPIKeyService;
import org.cerberus.core.util.StringUtil;
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

    @Autowired
    private IUserService userService;

    @Override
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        try {
            LOG.debug("Checking API Call.");

            if (this.isApiKeyAuthEnabled()) {

                // If already aauthorised, we don't need to check the api key.
                LOG.debug(request.getUserPrincipal());
                if ((request.getUserPrincipal() != null) && (!StringUtil.isEmptyOrNull(request.getUserPrincipal().getName()))) {
                    LOG.debug("User connected with : '" + request.getUserPrincipal().getName() + "'");
                    return true;
                }

                String apiKey = request.getHeader("apikey");

                if (apiKeyValidLogin(apiKey) != null) {
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
        return isApiKeyAuthEnabled() && apiKeyValidLogin(apiKey) != null;
    }

    @Override
    public boolean authenticate(Principal principal, String apiKey) {
        return (principal != null && !StringUtil.isEmptyOrNull(principal.getName())) || this.authenticate(apiKey);
    }

    @Override
    public String authenticateLogin(Principal principal, String apiKey) {
        if (principal != null && !StringUtil.isEmptyOrNull(principal.getName())) {
            return principal.getName();
        }
        if  (isApiKeyAuthEnabled()) {
            return apiKeyValidLogin(apiKey);
        }
        return null;
    }
    
    private boolean isApiKeyAuthEnabled() {
        return parameterService.getParameterBooleanByKey(Parameter.VALUE_cerberus_apikey_enable, "", true);
    }

    @Override
    public String getServiceAccountAPIKey() {
        try {
            return userService.findUserByKey(User.CERBERUS_SERVICEACCOUNT_LOGIN).getApiKey();
        } catch (CerberusException ex) {
            LOG.error("Error when trying to get APIKey of service account : " + User.CERBERUS_SERVICEACCOUNT_LOGIN);
        }
        return null;
    }

    private String apiKeyValidLogin(String apiKey) {
        String login = null;
        if (!StringUtil.isEmptyOrNull(apiKey)) {
            login = userService.verifyAPIKey(apiKey);
        }
        return login;

    }

}
