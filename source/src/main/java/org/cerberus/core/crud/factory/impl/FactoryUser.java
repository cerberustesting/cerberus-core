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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import java.util.List;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.entity.UserSystem;
import org.cerberus.core.crud.factory.IFactoryUser;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryUser implements IFactoryUser {

    @Override
    public User create(int userID, String login, String password, String resetPasswordToken, String request, String name, String team, String language,
            String reportingFavorite, String robotHost, String robotPort,
            String robotPlatform, String robotBrowser, String robotVersion, String robot, String defaultSystem, String email, List<UserSystem> userSystems, List<UserRole> userGroups) {
        User newUser = new User();
        newUser.setUserID(userID);
        newUser.setLogin(login);
        newUser.setPassword(password);
        newUser.setRequest(request);
        newUser.setName(name);
        newUser.setTeam(team);
        newUser.setLanguage(language);
        newUser.setReportingFavorite(reportingFavorite);
        newUser.setRobotHost(robotHost);
        newUser.setDefaultSystem(defaultSystem);
        newUser.setEmail(email);
        newUser.setRobotBrowser(robotBrowser);
        newUser.setRobotPlatform(robotPlatform);
        newUser.setRobotPort(robotPort);
        newUser.setRobotVersion(robotVersion);
        newUser.setRobot(robot);
        newUser.setUserSystems(userSystems);
        newUser.setUserRoles(userGroups);
        newUser.setResetPasswordToken(resetPasswordToken);
        return newUser;
    }

    @Override
    public User create(int userID, String login, String password, String resetPasswordToken, String request, String name, String team, String language,
            String reportingFavorite, String robotHost, String robotPort,
            String robotPlatform, String robotBrowser, String robotVersion, String robot, String defaultSystem, String email, String userPreferences,
            String attribute01, String attribute02, String attribute03, String attribute04, String attribute05,
            String comment, String apiKey,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif
    ) {
        User newUser = new User();
        newUser.setUserID(userID);
        newUser.setLogin(login);
        newUser.setPassword(password);
        newUser.setRequest(request);
        newUser.setName(name);
        newUser.setTeam(team);
        newUser.setLanguage(language);
        newUser.setReportingFavorite(reportingFavorite);
        newUser.setRobotHost(robotHost);
        newUser.setDefaultSystem(defaultSystem);
        newUser.setEmail(email);
        newUser.setRobotBrowser(robotBrowser);
        newUser.setRobotPlatform(robotPlatform);
        newUser.setRobotPort(robotPort);
        newUser.setRobotVersion(robotVersion);
        newUser.setRobot(robot);
        newUser.setUserPreferences(userPreferences);
        newUser.setResetPasswordToken(resetPasswordToken);

        newUser.setAttribute01(attribute01);
        newUser.setAttribute02(attribute02);
        newUser.setAttribute03(attribute03);
        newUser.setAttribute04(attribute04);
        newUser.setAttribute05(attribute05);
        newUser.setComment(comment);
        newUser.setApiKey(apiKey);
        newUser.setUsrCreated(usrCreated);
        newUser.setDateCreated(dateCreated);
        newUser.setUsrModif(usrModif);
        newUser.setDateModif(dateModif);

        return newUser;
    }
}
