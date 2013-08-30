/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.MyVersion;
import com.redcats.tst.factory.IFactoryMyversion;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryMyversion implements IFactoryMyversion {

    @Override
    public MyVersion create(String key, int value) {
        MyVersion newMyVersion = new MyVersion();
        newMyVersion.setKey(key);
        newMyVersion.setValue(value);

        return newMyVersion;

    }

}
