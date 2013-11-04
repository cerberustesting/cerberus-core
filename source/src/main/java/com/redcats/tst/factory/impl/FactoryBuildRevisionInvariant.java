/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.BuildRevisionInvariant;
import com.redcats.tst.factory.IFactoryBuildRevisionInvariant;
import org.springframework.stereotype.Service;


@Service
public class FactoryBuildRevisionInvariant implements IFactoryBuildRevisionInvariant {

    @Override
    public BuildRevisionInvariant create(String system, Integer level, Integer seq, String versionName) {
        BuildRevisionInvariant newBuildRevisionInvariant = new BuildRevisionInvariant();
        newBuildRevisionInvariant.setSystem(system);
        newBuildRevisionInvariant.setLevel(level);
        newBuildRevisionInvariant.setSeq(seq);
        newBuildRevisionInvariant.setVersionName(versionName);
        return newBuildRevisionInvariant;
    }
    
}
