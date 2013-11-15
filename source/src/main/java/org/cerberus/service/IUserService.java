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
package org.cerberus.service;

import org.cerberus.entity.User;
import org.cerberus.exception.CerberusException;

import java.util.List;

/**
 * @author vertigo
 */
public interface IUserService {

    /**
     * @param login
     * @return the user that match the login
     * @throws CerberusException
     */
    User findUserByKey(String login) throws CerberusException;

    /**
     * @return a list of all the users
     * @throws CerberusException
     */
    List<User> findallUser() throws CerberusException;

    /**
     * @param user
     * @return
     * @throws CerberusException
     */
    void insertUser(User user) throws CerberusException;

    /**
     * @param user
     * @return
     * @throws CerberusException
     */
    void deleteUser(User user) throws CerberusException;

    /**
     * @param user
     * @throws CerberusException
     */
    void updateUser(User user) throws CerberusException;

    /**
     * @param user
     * @param currentPassword
     * @param newPassword
     * @param confirmPassword
     * @return
     * @throws CerberusException
     */
    User updateUserPassword(User user, String currentPassword, String newPassword, String confirmPassword) throws CerberusException;

    /**
     * @param user
     * @param password
     * @return
     */
    boolean verifyPassword(User user, String password);

    /**
     *
     * @param User
     * @return true if user exist. false if not.
     */
    boolean isUserExist(String user);
}
