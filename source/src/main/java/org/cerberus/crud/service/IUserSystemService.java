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
package org.cerberus.crud.service;

import java.util.List;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IUserSystemService {
    
    UserSystem findUserSystemByKey(String login, String system) throws CerberusException;

    /**
     * @return a list of all the userSystem
     * @throws CerberusException
     */
    List<UserSystem> findallUser() throws CerberusException;

    /**
     * @param login
     * @return a list of all the userSystem of a user
     * @throws CerberusException
     */
    List<UserSystem> findUserSystemByUser(String login) throws CerberusException;
    
    /**
     * @param system
     * @return a list of all the userSystem of a system
     * @throws CerberusException
     */
    List<UserSystem> findUserSystemBySystem(String system) throws CerberusException;

    /**
     * @param userSystem
     * @throws CerberusException
     */
    void insertUserSystem(UserSystem userSystem) throws CerberusException;

    /**
     * @param userSystem
     * @throws CerberusException
     */
    void deleteUserSystem(UserSystem userSystem) throws CerberusException;

    /**
     * @param user
     * @param newSystems
     * @throws CerberusException
     */
    void updateUserSystems(User user, List<UserSystem> newSystems) throws CerberusException;

    /**
     * @param login
     * @return a list of all the userSystem of a user
     */
    AnswerList<UserSystem> readByUser(String login);

    /**
     * @param user
     * @param newGroups
     * @return
     */
    Answer updateSystemsByUser(User user, List<UserSystem> newGroups);

    /**
     *
     * @param user
     * @throws CerberusException
     */
    void createSystemAutomatic(String user) throws CerberusException;
    
    /**
     * Adding the usersystem
     *
     * @param sys
     * @return
     */
    Answer create(UserSystem sys);

    /**
     * Remove the usersystem
     *
     * @param sys
     * @return
     */
    Answer remove(UserSystem sys);
    
}
