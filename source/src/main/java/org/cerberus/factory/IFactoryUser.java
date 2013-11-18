/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.factory;

import org.cerberus.entity.User;

/**
 * @author vertigo
 */
public interface IFactoryUser {

    /**
     * @param userID            Internal ID of the user
     * @param login             login name of the user.
     * @param password          Password of the user
     * @param request           Y if the user needs to change the password on next login
     * @param name              Name of the user
     * @param team              Team the user belong to.
     * @param reportingFavorite Default parameters for reporting.
     * @param defaultIP         Default IP used for Selenium sApplicationerver.
     * @param defaultSystem     Default System of the user.
     * @return A User.
     */
    User create(int userID, String login, String password, String request, String name, String team, String reportingFavorite, String defaultIP, String defaultSystem);

}
