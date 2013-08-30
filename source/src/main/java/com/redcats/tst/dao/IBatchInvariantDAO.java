package com.redcats.tst.dao;

import com.redcats.tst.entity.BatchInvariant;
import com.redcats.tst.exception.CerberusException;

/**
 * @author bdumont
 */
public interface IBatchInvariantDAO {

    BatchInvariant findBatchInvariantByKey(String batch) throws CerberusException;
}
