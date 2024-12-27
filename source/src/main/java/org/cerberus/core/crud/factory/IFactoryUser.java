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
package org.cerberus.core.crud.factory;

import java.sql.Timestamp;
import java.util.List;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.entity.UserSystem;

/**
 * @author vertigo
 */
public interface IFactoryUser {

    /**
     * @param userID Internal ID of the user
     * @param login login name of the user.
     * @param password Password of the user
     * @param resetPasswordToken
     * @param request Y if the user needs to change the password on next login
     * @param name Name of the user
     * @param team Team the user belong to.
     * @param language Gui Language of the user.
     * @param reportingFavorite Default parameters for reporting.
     * @param robotHost Host used by Robot.
     * @param robotPort
     * @param robotPlatform
     * @param robotBrowser
     * @param defaultSystem Default System of the user.
     * @param robotVersion
     * @param robot
     * @param email email of the user
     * @param userSystems List of system allowed to the user
     * @param userGroups List of group of the user
     * @return A User.
     */
    User create(int userID, String login, String password, String resetPasswordToken, String request, String name,
            String team, String language, String reportingFavorite, String robotHost, String robotPort,
            String robotPlatform, String robotBrowser,
            String robotVersion, String robot, String defaultSystem, String email, List<UserSystem> userSystems, List<UserRole> userGroups);

    /**
     * @param userID Internal ID of the user
     * @param login login name of the user.
     * @param password Password of the user
     * @param resetPasswordToken
     * @param request Y if the user needs to change the password on next login
     * @param name Name of the user
     * @param team Team the user belong to.
     * @param language Gui Language of the user.
     * @param reportingFavorite Default parameters for reporting.
     * @param robotHost Host used by Robot.
     * @param robotPort
     * @param robotPlatform
     * @param robotBrowser
     * @param defaultSystem Default System of the user.
     * @param robotVersion
     * @param robot
     * @param email email of the user
     * @param userPreferences GUI user preferences
     * @param attribute01
     * @param attribute02
     * @param attribute03
     * @param attribute04
     * @param comment
     * @param attribute05
     * @param apiKey
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @return A User.
     */
    User create(int userID, String login, String password, String resetPasswordToken, String request, String name,
            String team, String language, String reportingFavorite, String robotHost, String robotPort,
            String robotPlatform, String robotBrowser,
            String robotVersion, String robot, String defaultSystem, String email, String userPreferences,
            String attribute01, String attribute02, String attribute03, String attribute04, String attribute05,
            String comment, String apiKey,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif
    );

}
