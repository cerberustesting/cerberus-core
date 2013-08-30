/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.DeployType;

/**
 * @author vertigo
 */
public interface IFactoryDeployType {

    DeployType create(String deployType, String description);
}
