/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Documentation;

/**
 *
 * @author bcivel
 */
public interface IFactoryDocumentation {
    
    Documentation create(String docTable,String docField,String docValue,String docLabel,String docDesc);
}
