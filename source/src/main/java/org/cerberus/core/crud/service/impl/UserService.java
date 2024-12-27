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
package org.cerberus.core.crud.service.impl;

import java.util.List;
import java.util.Map;

import org.cerberus.core.crud.dao.IUserDAO;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.crud.service.IUserService;
import org.cerberus.core.crud.service.IUserSystemService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.crud.service.IUserRoleService;

/**
 * @author vertigo
 */
@Service
public class UserService implements IUserService {

    @Autowired
    private IUserDAO userDAO;
    @Autowired
    private IUserRoleService userGroupService;
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
    public void insertUserNoAuth(User user) throws CerberusException {
        if (!userDAO.insertUserNoAuth(user)) {
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
    public AnswerItem<User> updateUserPassword(User user, String currentPassword, String newPassword, String confirmPassword, String resetPasswordToken) {
        AnswerItem<User> answUpdate = new AnswerItem<>();
        MessageEvent msg;
        //First check if both new password are the same
        if (newPassword.equals(confirmPassword)) {
            //Then check if resetPasswordToken is fed
            if (!resetPasswordToken.isEmpty()) {
                //Then check if token is the one in database
                if (verifyResetPasswordToken(user, resetPasswordToken)) {
                    //verifications succeed, update password
                    answUpdate = userDAO.updateUserPassword(user, newPassword, "N");
                    //Clear Token
                    userDAO.clearResetPasswordToken(user);
                    return answUpdate;
                } else {
                    //If token is invalid, raise an error
                    answUpdate.setItem(user);
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Reset Password Token is not valid!"));
                    answUpdate.setResultMessage(msg);
                    return answUpdate;
                }
            }

            //If resetPasswordToken empty, check if current password is correct
            if (this.verifyPassword(user, currentPassword)) {
                //verifications succeed
                answUpdate = userDAO.updateUserPassword(user, newPassword, "N");
            } else {
                //same user
                answUpdate.setItem(user);
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Current password is not valid!"));
                answUpdate.setResultMessage(msg);
            }
        } else {
            //same user            
            answUpdate.setItem(user);
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_VALIDATIONS_ERROR);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "New password confirmation failed! Please re-enter new password!"));
            answUpdate.setResultMessage(msg);
        }
        return answUpdate;
    }

    @Override
    public AnswerItem<User> updateUserPasswordAdmin(User user, String newPassword) {
        AnswerItem<User> answUpdate = new AnswerItem<>();
        MessageEvent msg;
        //verifications succeed, update password
        answUpdate = userDAO.updateUserPassword(user, newPassword, user.getRequest());
        return answUpdate;
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return userDAO.verifyPassword(user, password);
    }

    @Override
    public boolean verifyResetPasswordToken(User user, String token) {
        return userDAO.verifyResetPasswordToken(user, token);
    }

    @Override
    public String verifyAPIKey(String apiKey) {
        return userDAO.verifyAPIKey(apiKey);
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
        result.setUserRoles(userGroupService.findRoleByKey(login));
        result.setUserSystems(userSystemService.findUserSystemByUser(login));
        return result;
    }

    @Override
    public List<User> findAllUserBySystem(String system) {
        return this.userDAO.findAllUserBySystem(system);
    }

    @Override
    public AnswerItem<User> readByKey(String login) {
        return userDAO.readByKey(login);
    }

    @Override
    public AnswerList<User> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string) {
        return userDAO.readByCriteria(startPosition, length, columnName, sort, searchParameter, string);
    }

    @Override
    public AnswerList<User> readByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch) {
        return userDAO.readByCriteria(startPosition, length, columnName, sort, searchParameter, individualSearch);
    }

    @Override
    public boolean exist(String login) {
        AnswerItem objectAnswer = readByKey(login);
        return (objectAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) && (objectAnswer.getItem() != null); // Call was successfull and object was found.
    }

    @Override
    public Answer create(User user) {
        return userDAO.create(user);
    }

    @Override
    public Answer delete(User user) {
        if (User.CERBERUS_SERVICEACCOUNT_LOGIN.equals(user.getLogin())) {

            MessageEvent msg = new MessageEvent(MessageEventEnum.GENERIC_ERROR);
            msg.setDescription(msg.getDescription().replace("%REASON%", "You cannot delete Cerberus service account '" + User.CERBERUS_SERVICEACCOUNT_LOGIN + "'!"));
            return new Answer(msg);

        } else {
            return userDAO.delete(user);

        }
    }

    @Override
    public Answer update(User user) {
        if (User.CERBERUS_SERVICEACCOUNT_LOGIN.equals(user.getLogin()) && user.getApiKey().length() < 15) {

            MessageEvent msg = new MessageEvent(MessageEventEnum.GENERIC_ERROR);
            msg.setDescription(msg.getDescription().replace("%REASON%", "Cerberus service account '" + User.CERBERUS_SERVICEACCOUNT_LOGIN + "' must have an APIKey larger than 15 digits!"));
            return new Answer(msg);

        } else {
            return userDAO.update(user);
        }
    }

    @Override
    public User convert(AnswerItem<User> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<User> convert(AnswerList<User> answerList) throws CerberusException {
        if (answerList.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerList.getDataList();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public void convert(Answer answer) throws CerberusException {
        if (answer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return;
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public Answer requestResetPassword(User user) throws CerberusException {
        Answer answUpdate = new AnswerItem<>();
        MessageEvent msg;

        /**
         * Generate new Password and set the RestPasswordRequest user.
         */
        String newPassGenerated = StringUtil.getRandomString(40, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        user.setResetPasswordToken(newPassGenerated);

        /**
         * Update in database
         */
        answUpdate = userDAO.update(user);

        return answUpdate;
    }

}
