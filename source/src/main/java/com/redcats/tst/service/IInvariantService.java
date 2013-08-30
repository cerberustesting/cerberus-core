/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.Invariant;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface IInvariantService {
    
    Invariant findInvariantByIdValue(String idName, String value) throws CerberusException;
    
    List<Invariant> findListOfInvariantById(String idName) throws CerberusException;

    List<Invariant> findInvariantByIdGp1(String idName, String gp) throws CerberusException;
}
