package com.redcats.tst.dao;

import com.redcats.tst.entity.Parameter;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/03/2013
 * @since 2.0.0
 */
public interface IParameterDAO {

    Parameter findParameterByKey(String key) throws CerberusException;

    public List<Parameter> findAllParameter() throws CerberusException;
    
    public void updateParameter(Parameter parameter) throws CerberusException;

}
