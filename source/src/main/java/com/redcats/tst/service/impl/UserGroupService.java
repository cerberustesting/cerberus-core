package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IUserGroupDAO;
import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IUserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 14/08/2013
 * @since 2.0.0
 */
@Service
public class UserGroupService implements IUserGroupService {

    @Autowired
    private IUserGroupDAO userGroupDAO;

    @Override
    public void updateUserGroups(User user, List<Group> newGroups) throws CerberusException {

        List<Group> oldGroups = this.findGroupByKey(user.getLogin());

        //delete if don't exist in new
        for (Group old : oldGroups) {
            if (!newGroups.contains(old)) {
                this.removeGroupFromUser(old, user);
            }
        }
        //insert if don't exist in old
        for (Group group : newGroups) {
            if (!oldGroups.contains(group)) {
                this.addGroupToUser(group, user);
            }
        }
    }

    private void addGroupToUser(Group group, User user) throws CerberusException {
        if (!userGroupDAO.addGroupToUser(group, user)) {
            //TODO define message => error occur trying to add group user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    private void removeGroupFromUser(Group group, User user) throws CerberusException {
        if (!userGroupDAO.removeGroupFromUser(group, user)) {
            //TODO define message => error occur trying to delete group user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
    }

    @Override
    public List<Group> findGroupByKey(String login) throws CerberusException {
        List<Group> list = userGroupDAO.findGroupByKey(login);
        if (list == null) {
            //TODO define message => error occur trying to find group user
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return list;
    }
}
