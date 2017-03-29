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
package org.cerberus.crud.factory.impl;

import java.util.List;
import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.crud.factory.IFactoryUser;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryUser implements IFactoryUser {

    @Override
    public User create(int userID, String login, String password, String resetPasswordToken, String request, String name, String team, String language,
            String reportingFavorite, String robotHost, String robotPort,
            String robotPlatform, String robotBrowser, String robotVersion, String robot, String defaultSystem, String email, List<UserSystem> userSystems, List<UserGroup> userGroups) {
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
        newUser.setUserGroups(userGroups);
        newUser.setResetPasswordToken(resetPasswordToken);
        return newUser;
    }

    @Override
    public User create(int userID, String login, String password, String resetPasswordToken, String request, String name, String team, String language,
            String reportingFavorite, String robotHost, String robotPort,
            String robotPlatform, String robotBrowser, String robotVersion, String robot, String defaultSystem, String email, String userPreferences) {
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
        return newUser;
    }
}
