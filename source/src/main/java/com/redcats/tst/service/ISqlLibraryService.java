/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.SqlLibrary;
import com.redcats.tst.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface ISqlLibraryService {

    SqlLibrary findSqlLibraryByKey(String name) throws CerberusException;
}
