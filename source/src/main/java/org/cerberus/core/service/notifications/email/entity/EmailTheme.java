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
package org.cerberus.core.service.notifications.email.entity;

/**
 * Branding applied to every templated email (white-label).
 *
 * @param appName application name displayed in the header
 * @param logoUrl absolute URL of the logo (empty = no logo)
 * @param primaryColor hex color of the header band and buttons
 * @param backgroundColor hex color of the page behind the card
 * @param footerText text displayed at the bottom of the mail
 * @param baseUrl base URL of the application, used to build links
 */
public record EmailTheme(String appName, String logoUrl, String primaryColor, String backgroundColor, String footerText, String baseUrl) {
}
