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
package org.cerberus.factory.impl;

import org.cerberus.entity.User;
import org.cerberus.factory.IFactoryUser;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryUser implements IFactoryUser {

    @Override
    public User create(int userID, String login, String password, String request, String name, String team, String reportingFavorite, String defaultIP, String defaultSystem) {
        User newUser = new User();
        newUser.setUserID(userID);
        newUser.setLogin(login);
        newUser.setPassword(password);
        newUser.setRequest(request);
        newUser.setName(name);
        newUser.setTeam(team);
        newUser.setReportingFavorite(reportingFavorite);
        newUser.setDefaultIP(defaultIP);
        newUser.setDefaultSystem(defaultSystem);
        return newUser;
    }
}
