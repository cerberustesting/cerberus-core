/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.BuildRevisionInvariant;

/**
 * @author vertigo
 */
public interface IFactoryBuildRevisionInvariant {

    BuildRevisionInvariant create(String system, Integer level, Integer seq, String versionName);
}
