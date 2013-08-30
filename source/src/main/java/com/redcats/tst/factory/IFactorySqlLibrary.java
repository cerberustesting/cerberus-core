/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.SqlLibrary;

/**
 *
 * @author bcivel
 */
public interface IFactorySqlLibrary {
    
    SqlLibrary create(String type,String name,String script,String description);
}
