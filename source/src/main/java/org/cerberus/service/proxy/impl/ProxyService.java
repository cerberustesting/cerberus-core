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
package org.cerberus.service.proxy.impl;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.service.proxy.IProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Utility class centralizing string utility methods
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
@Service
public class ProxyService implements IProxyService {

    @Autowired
    private IParameterService parameterService;

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProxyService.class);
    /**
     * Proxy default config. (Should never be used as default config is inserted
     * into database)
     */
    private static final boolean DEFAULT_PROXY_ACTIVATE = false;
    private static final String DEFAULT_PROXY_HOST = "proxy";
    private static final int DEFAULT_PROXY_PORT = 80;
    private static final boolean DEFAULT_PROXYAUTHENT_ACTIVATE = false;
    private static final String DEFAULT_PROXYAUTHENT_USER = "squid";
    private static final String DEFAULT_PROXYAUTHENT_PASSWORD = "squid";

    /**
     * Determine if the passed parameter boolean value
     *
     * @param targetUrl
     * @param system
     * @return true if parse is one of the following : Y, yes, true
     */
    @Override
    public boolean useProxy(String targetUrl, String system) {
        LOG.debug("Entering useProxy with TargetUrl : " + targetUrl);
        boolean result = parameterService.getParameterBooleanByKey("cerberus_proxy_active", system, DEFAULT_PROXY_ACTIVATE);
        if (!(result)) {
            LOG.debug("Proxy desactivated by config.");
            return result;
        }
        String noProxy = parameterService.getParameterStringByKey("cerberus_proxy_nonproxyhosts", system, "");
        String noProxyReg = convertToRegEx(noProxy);

        URI uri;
        try {
            uri = new URI(targetUrl);

            String hostname = uri.getHost();
            // to provide faultproof result, check if not null then return only hostname, without www.
            if (hostname != null) {
                // regex compilation
                Pattern p = Pattern.compile(noProxyReg);
                // matcher creation
                Matcher m = p.matcher(hostname);
                result = m.find();
                // matcher result
                LOG.debug("TargetUrl : " + targetUrl + " - NonProxyRegEx : " + noProxyReg + " Config cerberus_proxy_nonproxyhosts = " + noProxy);
                LOG.debug("Host : " + hostname + " - RegExMatch Result : " + result);
                return (!result);

            } else {
                LOG.debug("Not able to get host from URL : " + targetUrl);
                return false;
            }

        } catch (Exception ex) {
            LOG.debug(ex.toString());
            return false;
        }

    }

    /**
     * Check for numeric data type
     *
     * @param useProxy
     * @return true if str is a numeric value, else false
     */
    @Override
    public String convertToRegEx(String useProxy) {
        LOG.debug("Entering convertToRegEx with useProxy : " + useProxy);

        String result = useProxy;
        if (result.startsWith("*")) {
            result = result.substring(1);
        } else {
            result = "^" + result;
        }
        if (result.endsWith("*")) {
            result = result.substring(0, (result.length() - 1));
        } else {
            result = result + "$";
        }
        result = result.replace(" ", "");
        result = result.replace("*,*", "|");
        result = result.replace(",*", "$|");
        result = result.replace("*,", "|^");
        result = result.replace(",", "$|^");
        result = result.replace(".", "\\.");
        result = result.replace("*", ".*");
        LOG.debug("Leaving convertToRegEx with result : " + result);
        return result;
    }

}
