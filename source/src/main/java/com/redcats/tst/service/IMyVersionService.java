/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.MyVersion;

/**
 *
 * @author bdumont
 */
public interface IMyVersionService {
 
    /**
     *
     * @param key
     * @return MyVersion that correspond to the key.
     */
    MyVersion findMyVersionByKey(String key);
    
    /**
     *
     * @param myversion
     * @return true if the update was done. False in case there were an issue.
     */
    boolean UpdateMyVersionTable(MyVersion myversion);
    
}
