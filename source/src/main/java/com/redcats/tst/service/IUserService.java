/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.User;
import com.redcats.tst.exception.CerberusException;

import java.util.List;

/**
 * @author vertigo
 */
public interface IUserService {

    /**
     * @param login
     * @return the user that match the login
     * @throws CerberusException
     */
    User findUserByKey(String login) throws CerberusException;

    /**
     * @return a list of all the users
     * @throws CerberusException
     */
    List<User> findallUser() throws CerberusException;

    /**
     * @param user
     * @return
     * @throws CerberusException
     */
    void insertUser(User user) throws CerberusException;

    /**
     * @param user
     * @return
     * @throws CerberusException
     */
    void deleteUser(User user) throws CerberusException;

    /**
     * @param user
     * @throws CerberusException
     */
    void updateUser(User user) throws CerberusException;

    /**
     * @param user
     * @param currentPassword
     * @param newPassword
     * @param confirmPassword
     * @return
     * @throws CerberusException
     */
    User updateUserPassword(User user, String currentPassword, String newPassword, String confirmPassword) throws CerberusException;

    /**
     * @param user
     * @param password
     * @return
     */
    boolean verifyPassword(User user, String password);

    /**
     *
     * @param User
     * @return true if user exist. false if not.
     */
    boolean isUserExist(String user);
}
