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
package org.cerberus.core.service.notifications.email;

import java.util.Locale;
import java.util.Map;

public interface IEmailRenderer {

    /**
     * Render the HTML template emails/{template}.html inside the common branded layout.
     * The variables are exposed to the template, along with "theme" (EmailTheme) and "title".
     *
     * @param template template name, without path nor extension
     * @param title title displayed in the mail header
     * @param variables variables of the template
     * @param locale language of the recipient
     * @param system system used to read the theme
     * @return the HTML of the mail
     */
    String render(String template, String title, Map<String, Object> variables, Locale locale, String system);
}
