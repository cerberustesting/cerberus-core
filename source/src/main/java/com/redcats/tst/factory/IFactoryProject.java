/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Project;

/**
 *
 * @author bcivel
 */
public interface IFactoryProject {
    
    Project create(String idProject,String code,String description,String active,String dateCreation);
}
