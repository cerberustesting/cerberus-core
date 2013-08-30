package com.redcats.tst.service;

import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 14/08/2013
 * @since 2.0.0
 */
public interface IUserGroupService {

    /**
     * @param user      the user to update the group
     * @param newGroups the user list of Groups
     * @throws CerberusException
     */
    void updateUserGroups(User user, List<Group> newGroups) throws CerberusException;

    /**
     * @param login the login of user
     * @return the user groups that match the login
     * @throws CerberusException
     */
    List<Group> findGroupByKey(String login) throws CerberusException;
}
