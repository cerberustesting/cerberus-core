/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Parameter;

/**
 *
 * @author bcivel
 */
public interface IFactoryParameter {
    
    Parameter create(String param,String value,String description);
}
