package com.redcats.tst.serviceEngine;


import com.redcats.tst.exception.CerberusEventException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 15/01/2013
 * @since 2.0.0
 */
public interface IConnectionPoolDAO {

    List<String> queryDatabase(String connectionName, String sql, int limit) throws CerberusEventException;
}
