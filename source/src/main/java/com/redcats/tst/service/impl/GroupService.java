/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IGroupDAO;
import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IGroupService;
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
