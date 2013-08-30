/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.SqlLibrary;
import com.redcats.tst.factory.IFactorySqlLibrary;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactorySqlLibrary implements IFactorySqlLibrary {

    @Override
    public SqlLibrary create(String type, String name, String script, String description) {
        SqlLibrary sqlLibrary = new SqlLibrary();
        sqlLibrary.setName(name);
        sqlLibrary.setScript(script);
        sqlLibrary.setType(type);
        sqlLibrary.setDescription(description);
        return sqlLibrary;
    }

}
