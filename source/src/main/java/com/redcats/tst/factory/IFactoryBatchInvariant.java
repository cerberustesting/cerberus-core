/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.BatchInvariant;

/**
 * @author vertigo
 */
public interface IFactoryBatchInvariant {

    BatchInvariant create(String batch, String incIni, String unit, String description);
}
