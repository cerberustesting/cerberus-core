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

import org.cerberus.dao.IGroupDAO;
import org.cerberus.entity.Group;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.entity.MessageGeneralEnum;
import org.cerberus.entity.User;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IGroupService;
import java.util.List;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vertigo
 */
@Service
public class GroupService implements IGroupService {

    @Autowired
    private IGroupDAO GroupDAO;

    @Override
    public List<Group> findGroupByUser(User user) {
        return GroupDAO.findGroupByUser(user);
    }

    @Override
    public List<Group> findallGroup() throws CerberusException {
        MyLogger.log(GroupService.class.getName(), Level.ERROR, "TO BE IMPLEMENTED - findallGroup");
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NOT_IMPLEMEMTED));
    }

    @Override
    public void insertGroupToUser(Group group, User user) throws CerberusException {
        MyLogger.log(GroupService.class.getName(), Level.ERROR, "TO BE IMPLEMENTED - insertGroupToUser");
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NOT_IMPLEMEMTED));
    }

    @Override
    public void deleteGroupFromUser(Group group, User user) throws CerberusException {
        MyLogger.log(GroupService.class.getName(), Level.ERROR, "TO BE IMPLEMENTED - deleteGroupFromUser");
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NOT_IMPLEMEMTED));
    }

    @Override
    public User updateGroupListToUser(List<Group> listGroup, User user) throws CerberusException {
        MyLogger.log(GroupService.class.getName(), Level.ERROR, "TO BE IMPLEMENTED - updateGroupListToUser");
        throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NOT_IMPLEMEMTED));
    }
}
