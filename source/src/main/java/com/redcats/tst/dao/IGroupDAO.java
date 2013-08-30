/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao;

import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.User;
import java.util.List;

/**
 *
 * @author vertigo
 */
public interface IGroupDAO {

    /**
     *
     * @param user
     * @return
     */
    List<Group> findGroupByUser(User user);
}
