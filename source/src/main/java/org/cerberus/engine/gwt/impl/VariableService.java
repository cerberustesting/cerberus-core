package org.cerberus.engine.gwt.impl;

import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.engine.gwt.IVariableService;
import org.cerberus.exception.CerberusEventException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by corentin on 20/10/16.
 */
@Service
public class VariableService implements IVariableService {
    @Autowired
    PropertyService propertyService;
    @Autowired
    ApplicationObjectVariableService applicationObjectVariableService;

    @Override
    public String decodeVariableWithExistingObject(String stringToDecode, TestCaseStepActionExecution testCaseStepActionExecution, boolean forceCalculation) throws CerberusEventException {
        String result = stringToDecode;
        result = propertyService.decodeValueWithExistingProperties(result,testCaseStepActionExecution,forceCalculation);
        result = applicationObjectVariableService.decodeValueWithExistingProperties(result,testCaseStepActionExecution,forceCalculation);
        return result;
    }
}
