/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IUserDAO;
import com.redcats.tst.dao.IUserGroupDAO;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author vertigo
 */
@Service
public class UserService implements IUserService {

    @Autowired
    private IUserDAO userDAO;

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
    public User updateUserPassword(User user, String currentPassword, String newPassword, String confirmPassword) throws CerberusException {
        User newUser;
        if (newPassword.equals(confirmPassword)) {
            if (this.verifyPassword(user, currentPassword)) {
                newUser = userDAO.updateUserPassword(user, newPassword);
                return newUser;
            } else {
                return user;
            }
        } else {
            return user;
        }
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return userDAO.verifyPassword(user, password);
    }
}
