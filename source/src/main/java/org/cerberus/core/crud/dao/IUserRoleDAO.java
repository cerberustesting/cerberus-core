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
package org.cerberus.core.crud.dao;

import java.util.List;

import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 09/08/2013
 * @since 2.0.0
 */
public interface IUserRoleDAO {

    /**
     * Adding the group to the user
     *
     * @param group
     * @param user
     * @return true if remove successfully amd false if an error occur
     */
    public boolean addRoleToUser(UserRole group, User user);

    /**
     * Remove the group from the user.
     *
     * @param group
     * @param user
     * @return true if remove successfully amd false if an error occur
     */
    public boolean removeRoleFromUser(UserRole group, User user);

    /**
     * @param login
     * @return a list of group user that correspond to the login.
     */
    public List<UserRole> findRoleByKey(String login);


    /**
     * @param login
     * @return a list of all the userSystem of a user
     */
    AnswerList<UserRole> readByUser(String login);

    /**
     * Adding the group
     *
     * @param group
     * @return
     */
    Answer create(UserRole group);

    /**
     * Remove the group
     *
     * @param group
     * @return
     */
    Answer remove(UserRole group);

    /**
     * Remove the group from the user.
     *
     * @param group
     * @param user
     * @return true if remove successfully amd false if an error occur
     */
    Answer removeRoleByUser(UserRole group, User user);
}
