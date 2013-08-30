/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Parameter;
import com.redcats.tst.factory.IFactoryParameter;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryParameter implements IFactoryParameter {

    @Override
    public Parameter create(String param, String value, String description) {
        Parameter parameter = new Parameter();
        parameter.setValue(value);
        parameter.setParam(param);
        parameter.setDescription(description);
        return parameter;
    }

}
