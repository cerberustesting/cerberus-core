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
import java.util.Map;

import org.cerberus.core.crud.entity.User;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

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
     * @throws CerberusException
     */
    void insertUser(User user) throws CerberusException;

    /**
     * @param user
     * @throws CerberusException
     */
    void insertUserNoAuth(User user) throws CerberusException;

    /**
     * @param user
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
     * @param resetPasswordToken
     * @return
     */
    AnswerItem<User> updateUserPassword(User user, String currentPassword, String newPassword, String confirmPassword, String resetPasswordToken);

    /**
     * @param user
     * @param newPassword
     * @return
     */
    AnswerItem<User> updateUserPasswordAdmin(User user, String newPassword);

    /**
     * @param user
     * @param password
     * @return
     */
    boolean verifyPassword(User user, String password);

    /**
     * @param user
     * @param password
     * @return
     */
    boolean verifyResetPasswordToken(User user, String password);

    /**
     * @param apiKey
     * @return
     */
    String verifyAPIKey(String apiKey);

    /**
     *
     * @param user
     * @return true if user exist. false if not.
     */
    boolean isUserExist(String user);

    /**
     *
     * @param start first row of the resultSet
     * @param amount number of row of the resultSet
     * @param column order the resultSet by this column
     * @param dir Asc or desc, information for the order by command
     * @param searchTerm search term on all the column of the resultSet
     * @param individualSearch search term on a dedicated column of the
     * resultSet
     * @return
     */
    List<User> findUserListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    /**
     *
     * @param searchTerm words to be searched in every column (Exemple :
     * article)
     * @param inds part of the script to add to where clause (Exemple : `type` =
     * 'Article')
     * @return The number of records for these criterias
     */
    Integer getNumberOfUserPerCrtiteria(String searchTerm, String inds);

    /**
     * @param login
     * @return the user that match the login
     * @throws CerberusException
     */
    User findUserByKeyWithDependencies(String login) throws CerberusException;

    /**
     *
     * @param system
     * @return
     */
    List<User> findAllUserBySystem(String system);

    /**
     *
     * @param login
     * @return
     */
    AnswerItem<User> readByKey(String login);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param string
     * @return
     */
    AnswerList<User> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);

    /**
     *
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<User> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param login
     * @return
     */
    boolean exist(String login);

    /**
     *
     * @param user
     * @return
     */
    Answer create(User user);

    /**
     *
     * @param user
     * @return
     */
    Answer delete(User user);

    /**
     *
     * @param user
     * @return
     */
    Answer update(User user);

    /**
     *
     * @param answerItem
     * @return
     * @throws CerberusException
     */
    User convert(AnswerItem<User> answerItem) throws CerberusException;

    /**
     *
     * @param answerList
     * @return
     * @throws CerberusException
     */
    List<User> convert(AnswerList<User> answerList) throws CerberusException;

    /**
     *
     * @param answer
     * @throws CerberusException
     */
    void convert(Answer answer) throws CerberusException;

    /**
     * @param user
     * @param newGeneratedPassword
     * @return
     * @throws CerberusException
     */
    Answer requestResetPassword(User user) throws CerberusException;

}
