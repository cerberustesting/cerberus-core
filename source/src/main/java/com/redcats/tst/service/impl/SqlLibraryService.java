/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.impl.SqlLibraryDAO;
import com.redcats.tst.entity.SqlLibrary;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.ISqlLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class SqlLibraryService implements ISqlLibraryService {

    @Autowired
    SqlLibraryDAO sqlLibraryDao;

    @Override
    public SqlLibrary findSqlLibraryByKey(String name) throws CerberusException {
        return sqlLibraryDao.findSqlLibraryByKey(name);
    }
}
