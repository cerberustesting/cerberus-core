/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Test;
import com.redcats.tst.factory.IFactoryTest;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTest implements IFactoryTest {

    @Override
    public Test create(String test, String description, String active, String automated, String tDateCrea) {
        Test newTest = new Test();
        newTest.setTest(test);
        newTest.setDescription(description);
        newTest.setActive(active);
        newTest.setAutomated(automated);
        newTest.settDateCrea(tDateCrea);

        return newTest;

    }
}
