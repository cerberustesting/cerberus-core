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
package org.cerberus.engine.entity;

import java.util.regex.Pattern;

/**
 *
 * @author bcivel
 */
public class Identifier {

    /**
     * The list of identifiers that can be used with its allowed pattern value if necessary
     */
    public interface Identifiers {
        /**
         * The coordinates key
         */
        String COORDINATE = "coord";

        /**
         * The coordinate value pattern
         */
        Pattern COORDINATE_VALUE_PATTERN = Pattern.compile(
                "x:(?<xCoordinate>\\d+),y:(?<yCoordinate>\\d+)",
                        // Can be used either as x:10,y:20 or as X:10,Y:20
                        Pattern.CASE_INSENSITIVE
                        // Just to allow unclosed spaces (even if # start-character is also allowed but not relevant here)
                        | Pattern.COMMENTS
        );
    }

    private String identifier;
    private String locator;
    
    /**
     * Identifier String.
     */
    public static final String IDENTIFIER_ID = "id";
    public static final String IDENTIFIER_NAME = "name";
    public static final String IDENTIFIER_CLASS = "class";
    public static final String IDENTIFIER_CSS = "css";
    public static final String IDENTIFIER_PICTURE = "picture";
    public static final String IDENTIFIER_XPATH = "xpath";
    public static final String IDENTIFIER_LINK = "link";
    public static final String IDENTIFIER_DATACERBERUS = "data-cerberus";
    public static final String IDENTIFIER_TITLE = "title";
    public static final String IDENTIFIER_REGEXTITLE = "regexTitle";
    public static final String IDENTIFIER_URL = "url";
    

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    /**
     * Check if the current {@link Identifier} is equal to the given identifier key (the {@link Identifier#getIdentifier()}
     *
     * @param identifier the identifier key to check
     * @return <code>true</code> if the current {@link Identifier}'s identifier key is equal to the given one, <code>false</code> otherwise
     */
    public boolean isSameIdentifier(final String identifier) {
        return identifier != null && identifier.equals(this.getIdentifier());
    }

    @Override
    public String toString() {
        return this.getIdentifier() + "=" + this.getLocator();
    }

}
