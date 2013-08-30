/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.DeployType;
import com.redcats.tst.factory.IFactoryDeployType;
import org.springframework.stereotype.Service;

@Service
public class FactoryDeployType implements IFactoryDeployType {

    @Override
    public DeployType create(String deployType, String description) {
        DeployType newDeployType = new DeployType();
        newDeployType.setDeploytype(deployType);
        newDeployType.setDescription(description);
        return newDeployType;
    }
}
