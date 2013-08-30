package com.redcats.tst.dao;

import com.redcats.tst.entity.SqlLibrary;
import com.redcats.tst.exception.CerberusException;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 10/01/2013
 * @since 2.0.0
 */
public interface ISqlLibraryDAO {

    SqlLibrary findSqlLibraryByKey(String name) throws CerberusException;
}
