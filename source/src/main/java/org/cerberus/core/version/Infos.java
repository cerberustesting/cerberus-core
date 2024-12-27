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
package org.cerberus.core.version;

import java.util.ResourceBundle;

/**
 * Contains information about project.
 *
 * <p>
 * Singleton class to use thanks to the {@link #getInstance()} method.
 * </p>
 *
 * @author abourdon
 */
public final class Infos {

    /**
     * {@link ResourceBundle} to the <code>infos</code> translation file
     */
    public static final ResourceBundle TRANSLATOR_INFOS = ResourceBundle.getBundle("lang.infos");

    /**
     * Version separator
     */
    public static final String VERSION_SEPARATOR = "-";

    /**
     * Access to the single instance of this class
     */
    private static final Infos INSTANCE = new Infos();

    /**
     * Gets the single class instance
     *
     * @return the single class instance
     */
    public static Infos getInstance() {
        return INSTANCE;
    }

    /**
     * The project name
     */
    private String projectName;

    /**
     * The project version
     */
    private String projectVersion;

    /**
     * Concatenation between project name and version
     */
    private String projectNameAndVersion;

    /**
     * Project Build ID
     */
    private String projectBuildId;

    /**
     * Gets the project name
     *
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the project version
     *
     * @return the project version
     */
    public String getProjectVersion() {
        return projectVersion;
    }

    /**
     * Gets the concatenation between the project name and version
     *
     * @return the concatenation between the project name and version
     * @see #getProjectName()
     * @see #getProjectVersion()
     */
    public String getProjectNameAndVersion() {
        return projectNameAndVersion;
    }

    /**
     *
     * @return
     */
    public String getProjectBuildId() {
        return projectBuildId;
    }

    /**
     * Private constructor as singleton class
     */
    public Infos() {
        init();
    }

    /**
     * Initialisation process
     */
    private void init() {
        projectName = TRANSLATOR_INFOS.getString("project.name");
        projectVersion = TRANSLATOR_INFOS.getString("project.version");
        projectNameAndVersion = projectName + VERSION_SEPARATOR + projectVersion;
        projectBuildId = TRANSLATOR_INFOS.getString("project.build");
    }

}
