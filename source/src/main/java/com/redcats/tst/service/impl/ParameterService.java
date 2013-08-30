/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IParameterDAO;
import com.redcats.tst.entity.Parameter;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IParameterService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ParameterService implements IParameterService {

    @Autowired
    private IParameterDAO parameterDao;
    
    @Override
    public Parameter findParameterByKey(String key) throws CerberusException{
        return parameterDao.findParameterByKey(key);
    }
    
    @Override
    public List<Parameter> findAllParameter() throws CerberusException{
        return parameterDao.findAllParameter();
    }
    
    @Override
    public void updateParameter(Parameter parameter) throws CerberusException{
        parameterDao.updateParameter(parameter);
    }
}
