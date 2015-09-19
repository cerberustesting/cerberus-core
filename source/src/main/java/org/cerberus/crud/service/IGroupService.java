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
package org.cerberus.crud.service;

import java.util.List;

import org.cerberus.crud.entity.Group;
import org.cerberus.crud.entity.User;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author vertigo
 */
public interface IGroupService {

    /**
     *
     * @param user
     * @return the list of group the user belong it return null if no groups are
     * found.
     */
    List<Group> findGroupByUser(User user);

    /**
     *
     * @return a list of all groups that exist.
     * @throws CerberusException if no groups exist.
     */
    List<Group> findallGroup() throws CerberusException;

    /**
     *
     * @param user the user that will be modified.
     * @param group, the group that will be inserted to the user.
     * @throws CerberusException if insert could not be done (already exist).
     */
    void insertGroupToUser(Group group, User user) throws CerberusException;

    /**
     *
     * @param user the user that will be modified.
     * @param group the group that will be removed from the user.
     * @return
     * @throws CerberusException if the link does not exist.
     */
    void deleteGroupFromUser(Group group, User user) throws CerberusException;

    /**
     *
     * @param user The user that will be updated.
     * @param listGroup The list of group that will overwrite the existing list
     * of group of the user
     * @return the list
     * @throws CerberusException if the user does not exist and list of group
     * could not be updated.
     */
    User updateGroupListToUser(List<Group> listGroup, User user) throws CerberusException;
}
