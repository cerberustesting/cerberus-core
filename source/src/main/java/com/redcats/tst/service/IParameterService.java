/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.Parameter;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface IParameterService {

    Parameter findParameterByKey(String key) throws CerberusException;
    
    List<Parameter> findAllParameter() throws CerberusException;
    
    void updateParameter(Parameter parameter) throws CerberusException;
    
}
