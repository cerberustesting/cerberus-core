/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Group;

/**
 *
 * @author vertigo
 */
public interface IFactoryGroup {
    
    /**
     *
     * @param group
     * @return
     */
    Group create(String group);
        
}
