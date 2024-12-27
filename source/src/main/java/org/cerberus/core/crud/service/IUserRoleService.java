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
package org.cerberus.core.crud.service;

import java.util.List;

import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 14/08/2013
 * @since 2.0.0
 */
public interface IUserRoleService {

    /**
     * @param user the user to update the role
     * @param newRoles the user list of Roles
     * @throws CerberusException
     */
    void updateUserRoles(User user, List<UserRole> newRoles) throws CerberusException;

    /**
     * @param login the login of user
     * @return the user roles that match the login
     * @throws CerberusException
     */
    List<UserRole> findRoleByKey(String login) throws CerberusException;

    /**
     * @param login
     * @return a list of all the userSystem of a user
     */
    AnswerList<UserRole> readByUser(String login);

    /**
     * @param user the user to update the role
     * @param newRoles the user list of Groups
     * @return 
     */
    Answer updateRolesByUser(User user, List<UserRole> newRoles);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    UserRole convert(AnswerItem<UserRole> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<UserRole> convert(AnswerList<UserRole> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
