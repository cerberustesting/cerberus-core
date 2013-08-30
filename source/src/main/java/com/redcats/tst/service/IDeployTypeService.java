/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.DeployType;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface IDeployTypeService {

    DeployType findDeployTypeByKey(String deploytype) throws CerberusException;
    
    List<DeployType> findAllDeployType() throws CerberusException;
}
