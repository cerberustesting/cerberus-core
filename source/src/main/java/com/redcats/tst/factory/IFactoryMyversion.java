/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.MyVersion;

/**
 * @author vertigo
 */
public interface IFactoryMyversion {


    /**
     * @param key   key for the version entry (ex: database for the database)
     * @param value value of the version.
     * @return MyVersion object Created
     */
    MyVersion create(String key, int value);

}
