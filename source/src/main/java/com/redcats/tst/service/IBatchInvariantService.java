/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.BatchInvariant;
import com.redcats.tst.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface IBatchInvariantService {

    BatchInvariant findBatchInvariantByKey(String batch) throws CerberusException;
}
