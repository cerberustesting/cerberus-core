/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

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
