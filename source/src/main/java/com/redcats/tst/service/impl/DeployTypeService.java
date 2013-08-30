/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IDeployTypeDAO;
import com.redcats.tst.entity.DeployType;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IDeployTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DeployTypeService implements IDeployTypeService {

    @Autowired
    private IDeployTypeDAO deployTypeDAO;

    @Override
    public DeployType findDeployTypeByKey(String deploytype) throws CerberusException {
        return deployTypeDAO.findDeployTypeByKey(deploytype);
    }

    @Override
    public List<DeployType> findAllDeployType() throws CerberusException {
        return deployTypeDAO.findAllDeployType();
    }
    
}
