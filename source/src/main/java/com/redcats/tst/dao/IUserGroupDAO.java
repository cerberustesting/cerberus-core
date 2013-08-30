package com.redcats.tst.dao;

import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.User;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 09/08/2013
 * @since 2.0.0
 */
public interface IUserGroupDAO {

    /**
     * Adding the group to the user
     *
     * @param group
     * @param user
     * @return true if remove successfully amd false if an error occur
     */
    public boolean addGroupToUser(Group group, User user);

    /**
     * Remove the group from the user.
     *
     * @param group
     * @param user
     * @return true if remove successfully amd false if an error occur
     */
    public boolean removeGroupFromUser(Group group, User user);

    /**
     * @param login
     * @return a list of group user that correspond to the login.
     */
    public List<Group> findGroupByKey(String login);
}
