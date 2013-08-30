/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Documentation;
import com.redcats.tst.factory.IFactoryDocumentation;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryDocumentation implements IFactoryDocumentation {

    @Override
    public Documentation create(String docTable, String docField, String docValue, String docLabel, String docDesc) {
        Documentation documentation = new Documentation();
        documentation.setDocTable(docTable);
        documentation.setDocField(docField);
        documentation.setDocValue(docValue);
        documentation.setDocLabel(docLabel);
        documentation.setDocDesc(docDesc);
        return documentation;
    }

}
