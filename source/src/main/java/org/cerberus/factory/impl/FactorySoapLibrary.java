/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cerberus.factory.impl;

import org.cerberus.entity.SoapLibrary;
import org.cerberus.factory.IFactorySoapLibrary;
import org.springframework.stereotype.Service;

/**
 *
 * @author cte
 */
@Service
public class FactorySoapLibrary implements IFactorySoapLibrary {
    
    @Override
    public SoapLibrary create(String type, String name, String envelope, String description, String servicePath, String parsingAnswer, String method) {
        SoapLibrary s = new SoapLibrary();
        s.setName(name);
        s.setEnvelope(envelope);
        s.setType(type);
        s.setDescription(description);
        s.setServicePath(servicePath);
        s.setParsingAnswer(parsingAnswer);
        s.setMethod(method);
        return s;
    }
}
