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

import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.crud.entity.UserRole;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.crud.entity.User;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cerberus.core.crud.service.IUserRoleService;
import org.cerberus.core.crud.dao.IUserRoleDAO;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 14/08/2013
 * @since 2.0.0
 */
@Service
public class UserRoleService implements IUserRoleService {

    @Autowired
    private IUserRoleDAO userRoleDAO;

    private final String OBJECT_NAME = "UserRole";

    @Override
    public void updateUserRoles(User user, List<UserRole> newRoles) throws CerberusException {

        List<UserRole> oldRoles = this.findRoleByKey(user.getLogin());

        //delete if don't exist in new
        for (UserRole old : oldRoles) {
            if (!newRoles.contains(old)) {
                this.removeRoleFromUser(old, user);
            }
        }
        //insert if don't exist in old
        for (UserRole role : newRoles) {
            if (!oldRoles.contains(role)) {
                this.addRoleToUser(role, user);
            }
        }
    }

    private void addRoleToUser(UserRole role, User user) throws CerberusException {
        if (!userRoleDAO.addRoleToUser(role, user)) {
            //TODO define message => error occur trying to add role user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    private void removeRoleFromUser(UserRole role, User user) throws CerberusException {
        if (!userRoleDAO.removeRoleFromUser(role, user)) {
            //TODO define message => error occur trying to delete role user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    @Override
    public List<UserRole> findRoleByKey(String login) throws CerberusException {
        List<UserRole> list = userRoleDAO.findRoleByKey(login);
        if (list == null) {
            //TODO define message => error occur trying to find role user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return list;
    }

    @Override
    public AnswerList<UserRole> readByUser(String login) {
        return userRoleDAO.readByUser(login);
    }

    @Override
    public Answer updateRolesByUser(User user, List<UserRole> newRoles) {
        Answer a = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                .resolveDescription("OPERATION", "UPDATE"));
        AnswerList<UserRole> an = this.readByUser(user.getLogin());
        if (an.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            List<UserRole> oldRoles = an.getDataList();
            //delete if don't exist in new
            for (UserRole old : oldRoles) {
                if (!newRoles.contains(old)) {
                    Answer del = userRoleDAO.remove(old);
                    if (!del.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        a = del;
                    }
                }
            }
            //insert if don't exist in old
            for (UserRole role : newRoles) {
                if (!oldRoles.contains(role)) {
                    Answer add = userRoleDAO.create(role);
                    if (!add.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        a = add;
                    }
                }
            }
        }
        return a;
    }

    @Override
    public UserRole convert(AnswerItem<UserRole> answerItem) throws CerberusException {
        if (answerItem.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            //if the service returns an OK message then we can get the item
            return answerItem.getItem();
        }
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.DATA_OPERATION_ERROR));
    }

    @Override
    public List<UserRole> convert(AnswerList<UserRole> answerList) throws CerberusException {
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

}
