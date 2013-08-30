/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Group;
import com.redcats.tst.factory.IFactoryGroup;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryGroup implements IFactoryGroup {

    @Override
    public Group create(String group) {
        Group newGroup = new Group();
        newGroup.setGroup(group);
        return newGroup;
    }
}
