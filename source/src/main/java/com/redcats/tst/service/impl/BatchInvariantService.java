/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IBatchInvariantDAO;
import com.redcats.tst.entity.BatchInvariant;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IBatchInvariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BatchInvariantService implements IBatchInvariantService {

    @Autowired
    private IBatchInvariantDAO batchInvariantDAO;
    
    @Override
    public BatchInvariant findBatchInvariantByKey(String batch) throws CerberusException {
        return batchInvariantDAO.findBatchInvariantByKey(batch);
    }
    
}
