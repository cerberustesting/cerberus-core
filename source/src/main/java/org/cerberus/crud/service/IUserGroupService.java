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

import org.cerberus.crud.entity.UserGroup;
import org.cerberus.crud.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 14/08/2013
 * @since 2.0.0
 */
public interface IUserGroupService {

    /**
     * @param user the user to update the group
     * @param newGroups the user list of Groups
     * @throws CerberusException
     */
    void updateUserGroups(User user, List<UserGroup> newGroups) throws CerberusException;

    /**
     * @param login the login of user
     * @return the user groups that match the login
     * @throws CerberusException
     */
    List<UserGroup> findGroupByKey(String login) throws CerberusException;

    /**
     * @param login
     * @return a list of all the userSystem of a user
     */
    AnswerList<UserGroup> readByUser(String login);

    /**
     * @param user the user to update the group
     * @param newGroups the user list of Groups
     * @return 
     */
    Answer updateGroupsByUser(User user, List<UserGroup> newGroups);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    UserGroup convert(AnswerItem<UserGroup> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<UserGroup> convert(AnswerList<UserGroup> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

}
