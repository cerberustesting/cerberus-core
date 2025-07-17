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
package org.cerberus.core.config.cerberus;

/**
 * Map la table Service
 *
 * @author cte
 */
public class Property {

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String AUTHENTIFICATION = "org.cerberus.authentification";
    public static final String KEYCLOAKREALM = "org.cerberus.keycloak.realm";
    public static final String KEYCLOAKCLIENT = "org.cerberus.keycloak.client";
    public static final String KEYCLOAKURL = "org.cerberus.keycloak.url";
    public static final String SAAS = "org.cerberus.saas";
    public static final String SAASINSTANCE = "org.cerberus.saas.instance";
    public static final String ENVIRONMENT = "org.cerberus.environment";

    public static final String AUTHENTIFICATION_VALUE_KEYCLOAK = "keycloak";

    public static boolean isKeycloak() {
        return ((System.getProperty(Property.AUTHENTIFICATION) != null) && (Property.AUTHENTIFICATION_VALUE_KEYCLOAK.equals(System.getProperty(Property.AUTHENTIFICATION))));
    }

    public static boolean isSaaS() {
        return ((System.getProperty(Property.SAAS) != null) && (("true".equals(System.getProperty(Property.SAAS))) || ("1".equals(System.getProperty(Property.SAAS)))));
    }

    private Property() {
    }

}
