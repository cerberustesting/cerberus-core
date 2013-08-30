package com.redcats.tst.dao;

import com.redcats.tst.entity.MyVersion;

/**
 * {Insert class description here}
 *
 * @author Benoit Dumont
 * @version 1.0, 09/06/2013
 * @since 2.0.0
 */
public interface IMyVersionDAO {

    /**
     *
     * @param key
     * @return
     */
    MyVersion findMyVersionByKey(String key);
    
    boolean updateMyVersion(MyVersion myVersion);
    
}
