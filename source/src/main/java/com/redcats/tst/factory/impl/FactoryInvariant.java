/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Invariant;
import com.redcats.tst.factory.IFactoryInvariant;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryInvariant implements IFactoryInvariant {

    @Override
    public Invariant create(String idName, String value, int sort, int id, String description, String gp1, String gp2, String gp3) {
        Invariant invariant = new Invariant();
        invariant.setIdName(idName);
        invariant.setSort(sort);
        invariant.setValue(value);
        invariant.setId(id);
        invariant.setDescription(description);
        invariant.setGp1(gp1);
        invariant.setGp2(gp2);
        invariant.setGp3(gp3);
        return invariant;
    }

}
