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
package org.cerberus.service.impl;

import java.util.List;
import org.cerberus.dao.IUserDAO;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IUserGroupService;
import org.cerberus.service.IUserService;
import org.cerberus.service.IUserSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class UserService implements IUserService {

    @Autowired
    private IUserDAO userDAO;
    @Autowired
    private IUserGroupService userGroupService;
    @Autowired
    private IUserSystemService userSystemService;

    @Override
    public User findUserByKey(String login) throws CerberusException {
        User user = userDAO.findUserByKey(login);
        if (user == null) {
            //TODO define message => error occur trying to find user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return user;
    }

    @Override
    public List<User> findallUser() throws CerberusException {
        List<User> users = userDAO.findAllUser();
        if (users == null) {
            //TODO define message => error occur trying to find users
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return users;
    }

    @Override
    public void insertUser(User user) throws CerberusException {
        if (!userDAO.insertUser(user)) {
            //TODO define message => error occur trying to find users
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    @Override
    public void deleteUser(User user) throws CerberusException {
        if (!userDAO.deleteUser(user)) {
            //TODO define message => error occur trying to delete user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    @Override
    public void updateUser(User user) throws CerberusException {
        if (!userDAO.updateUser(user)) {
            //TODO define message => error occur trying to update user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    @Override
    public User updateUserPassword(User user, String currentPassword, String newPassword, String confirmPassword) throws CerberusException {
        User newUser;
        if (newPassword.equals(confirmPassword)) {
            if (this.verifyPassword(user, currentPassword)) {
                newUser = userDAO.updateUserPassword(user, newPassword);
                return newUser;
            } else {
                return user;
            }
        } else {
            return user;
        }
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return userDAO.verifyPassword(user, password);
    }

    @Override
    public boolean isUserExist(String user) {
        try {
            findUserByKey(user);
            return true;
        } catch (CerberusException e) {
            return false;
        }
    }

    @Override
    public List<User> findUserListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return userDAO.findTestDataListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }

    @Override
    public Integer getNumberOfUserPerCrtiteria(String searchTerm, String inds) {
        return userDAO.getNumberOfUserPerCriteria(searchTerm, inds);
    }

    @Override
    public User findUserByKeyWithDependencies(String login) throws CerberusException {
        User result = this.findUserByKey(login);
        result.setUserGroups(userGroupService.findGroupByKey(login));
        result.setUserSystems(userSystemService.findUserSystemByUser(login));
        return result;
    }

    @Override
    public List<User> findAllUserBySystem(String system) {
        return this.userDAO.findAllUserBySystem(system);
    }
}
