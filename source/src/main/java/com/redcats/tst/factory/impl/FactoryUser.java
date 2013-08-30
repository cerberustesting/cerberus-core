/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.User;
import com.redcats.tst.factory.IFactoryUser;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryUser implements IFactoryUser {

    @Override
    public User create(int userID, String login, String password, String request, String name, String team, String reportingFavorite, String defaultIP, String defaultSystem) {
        User newUser = new User();
        newUser.setUserID(userID);
        newUser.setLogin(login);
        newUser.setPassword(password);
        newUser.setRequest(request);
        newUser.setName(name);
        newUser.setTeam(team);
        newUser.setReportingFavorite(reportingFavorite);
        newUser.setDefaultIP(defaultIP);
        newUser.setDefaultSystem(defaultSystem);
        return newUser;
    }
}
