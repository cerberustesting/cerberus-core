/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IGroupDAO;
import com.redcats.tst.entity.Group;
import com.redcats.tst.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GroupDAO implements IGroupDAO {

    @Override
    public List<Group> findGroupByUser(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
