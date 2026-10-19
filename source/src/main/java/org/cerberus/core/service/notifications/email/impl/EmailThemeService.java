/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.notifications.email.impl;

import java.util.regex.Pattern;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.service.notifications.email.IEmailThemeService;
import org.cerberus.core.service.notifications.email.entity.EmailTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailThemeService implements IEmailThemeService {

    public static final String PARAM_APPNAME = "cerberus_email_theme_appname";
    public static final String PARAM_LOGOURL = "cerberus_email_theme_logourl";
    public static final String PARAM_PRIMARYCOLOR = "cerberus_email_theme_primarycolor";
    public static final String PARAM_BACKGROUNDCOLOR = "cerberus_email_theme_backgroundcolor";
    public static final String PARAM_FOOTERTEXT = "cerberus_email_theme_footertext";

    static final String DEFAULT_APPNAME = "Cerberus Testing";
    static final String DEFAULT_PRIMARYCOLOR = "#2c7be5";
    static final String DEFAULT_BACKGROUNDCOLOR = "#f5f6fa";

    // Colors end up in inline styles : only accept hex values.
    private static final Pattern HEX_COLOR = Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");

    @Autowired
    private IParameterService parameterService;

    @Override
    public EmailTheme read(String system) {
        // The logo falls back on the instance logo already used by the pdf reports.
        String defaultLogo = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_instancelogo_url, system, "");
        // URL of the instance (cerberus_gui_url, then cerberus_url) : displayed in the footer to identify the sender.
        String baseUrl = parameterService.getParameterStringCerberusURLByKey();
        return new EmailTheme(
                parameterService.getParameterStringByKey(PARAM_APPNAME, system, DEFAULT_APPNAME),
                parameterService.getParameterStringByKey(PARAM_LOGOURL, system, defaultLogo),
                color(parameterService.getParameterStringByKey(PARAM_PRIMARYCOLOR, system, DEFAULT_PRIMARYCOLOR), DEFAULT_PRIMARYCOLOR),
                color(parameterService.getParameterStringByKey(PARAM_BACKGROUNDCOLOR, system, DEFAULT_BACKGROUNDCOLOR), DEFAULT_BACKGROUNDCOLOR),
                parameterService.getParameterStringByKey(PARAM_FOOTERTEXT, system, ""),
                baseUrl);
    }

    private static String color(String value, String defaultValue) {
        return value != null && HEX_COLOR.matcher(value.trim()).matches() ? value.trim() : defaultValue;
    }
}
