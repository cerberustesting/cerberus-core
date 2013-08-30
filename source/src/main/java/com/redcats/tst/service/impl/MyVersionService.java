/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IMyVersionDAO;
import com.redcats.tst.entity.MyVersion;
import com.redcats.tst.service.IMyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class MyVersionService implements IMyVersionService {

    @Autowired
    private IMyVersionDAO myVersionDAO;

    @Override
    public MyVersion findMyVersionByKey(String key) {
        return this.myVersionDAO.findMyVersionByKey(key);
    }

    @Override
    public boolean UpdateMyVersionTable(MyVersion myversion) {
        return this.myVersionDAO.updateMyVersion(myversion);
    }
}
