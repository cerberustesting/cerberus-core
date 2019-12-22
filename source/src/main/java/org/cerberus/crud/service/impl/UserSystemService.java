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
package org.cerberus.crud.service.impl;

import java.util.List;
import org.cerberus.crud.dao.IUserSystemDAO;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.crud.entity.User;
import org.cerberus.crud.entity.UserSystem;
import org.cerberus.crud.factory.IFactoryInvariant;
import org.cerberus.crud.factory.IFactoryUserSystem;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.IUserSystemService;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class UserSystemService implements IUserSystemService {

    @Autowired
    private IUserSystemDAO userSystemDAO;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private IInvariantService invariantService;
    @Autowired
    private IFactoryInvariant invariantFactory;
    @Autowired
    private IFactoryUserSystem factoryUserSystem;
    @Autowired
    private IUserService userService;

    private final String OBJECT_NAME = "UserSystem";

    @Override
    public UserSystem findUserSystemByKey(String login, String system) throws CerberusException {
        return userSystemDAO.findUserSystemByKey(login, system);
    }

    @Override
    public List<UserSystem> findallUser() throws CerberusException {
        return userSystemDAO.findallUser();
    }

    @Override
    public List<UserSystem> findUserSystemByUser(String login) throws CerberusException {
        return userSystemDAO.findUserSystemByUser(login);
    }

    @Override
    public List<UserSystem> findUserSystemBySystem(String system) throws CerberusException {
        return userSystemDAO.findUserSystemBySystem(system);
    }

    @Override
    public void insertUserSystem(UserSystem useSystem) throws CerberusException {
        userSystemDAO.insertUserSystem(useSystem);
    }

    @Override
    public void deleteUserSystem(UserSystem userSystem) throws CerberusException {
        userSystemDAO.deleteUserSystem(userSystem);
    }

    @Override
    public void updateUserSystems(User user, List<UserSystem> newSystems) throws CerberusException {
        List<UserSystem> oldSystems = this.findUserSystemByUser(user.getLogin());

        //delete if don't exist in new
        for (UserSystem old : oldSystems) {
            if (!newSystems.contains(old)) {
                this.deleteUserSystem(old);
            }
        }
        //insert if don't exist in old
        for (UserSystem newS : newSystems) {
            if (!oldSystems.contains(newS)) {
                this.insertUserSystem(newS);
            }
        }
    }

    @Override
    public AnswerList<UserSystem> readByUser(String login) {
        return userSystemDAO.readByUser(login);
    }

    @Override
    public void createSystemAutomatic(String user) throws CerberusException {
        // Automatically create a User Space system depending on parameters.
        if (parameterService.getParameterBooleanByKey("cerberus_accountcreation_ownsystemcreation", "", true)) {
            String newSystem = "US-" + user;
            // Create invariant.
            invariantService.create(invariantFactory.create("SYSTEM", newSystem, 9999, "System for user " + user, "User System", "", "", "", "", "", "", "", "", ""));
            // Create User/System.
            UserSystem us = factoryUserSystem.create(user, newSystem);
            userSystemDAO.create(us);
            // Update User to System.
            User myuser = userService.convert(userService.readByKey(user));
            myuser.setDefaultSystem(newSystem);
            userService.update(myuser);
        }
        // Automatically all systems depending on parameters.
        String param = parameterService.getParameterStringByKey("cerberus_accountcreation_systemlist", "", "ALL");
        if (param.equals("ALL")) {
            userSystemDAO.createAllSystemList(user);
        } else if (!param.equals("NONE")) {
            if (param.contains(",")) {
                String[] systemList = param.split(",");
                userSystemDAO.createSystemList(user, systemList);
            }
        }
    }

    @Override
    public Answer create(UserSystem sys) {
        return userSystemDAO.create(sys);
    }

    @Override
    public Answer remove(UserSystem sys) {
        return userSystemDAO.remove(sys);
    }

    @Override
    public Answer updateSystemsByUser(User user, List<UserSystem> newGroups) {
        Answer a = new Answer(new MessageEvent(MessageEventEnum.DATA_OPERATION_OK).resolveDescription("ITEM", OBJECT_NAME)
                .resolveDescription("OPERATION", "UPDATE"));

        AnswerList<UserSystem> an = this.readByUser(user.getLogin());
        if (an.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
            List<UserSystem> oldGroups = an.getDataList();
            //delete if don't exist in new
            for (UserSystem old : oldGroups) {
                if (!newGroups.contains(old)) {
                    Answer del = userSystemDAO.remove(old);
                    if (!del.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        a = del;
                    }
                }
            }
            //insert if don't exist in old
            for (UserSystem group : newGroups) {
                if (!oldGroups.contains(group)) {
                    Answer add = userSystemDAO.create(group);
                    if (!add.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        a = add;
                    }
                }
            }
        }
        return a;
    }

}
