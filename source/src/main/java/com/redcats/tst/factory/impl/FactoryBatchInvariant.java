/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.BatchInvariant;
import com.redcats.tst.factory.IFactoryBatchInvariant;
import org.springframework.stereotype.Service;

@Service
public class FactoryBatchInvariant implements IFactoryBatchInvariant {

    @Override
    public BatchInvariant create(String batch, String incIni, String unit, String description) {
        BatchInvariant newBatchInvariant = new BatchInvariant();
        newBatchInvariant.setBatch(batch);
        newBatchInvariant.setIncIni(incIni);
        newBatchInvariant.setUnit(unit);
        newBatchInvariant.setDescription(description);
        return newBatchInvariant;
    }
}
