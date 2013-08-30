package com.redcats.tst.dao;

import com.redcats.tst.entity.Invariant;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
public interface IInvariantDAO {

    Invariant findInvariantByIdValue(String idName, String value) throws CerberusException;

    List<Invariant> findListOfInvariantById(String idName) throws CerberusException;

    List<Invariant> findInvariantByIdGp1(String idName, String gp) throws CerberusException;
}
