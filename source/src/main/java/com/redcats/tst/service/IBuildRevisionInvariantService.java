/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.BuildRevisionInvariant;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface IBuildRevisionInvariantService {

    BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, Integer seq) throws CerberusException;

    BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, String versionName) throws CerberusException;

    List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystemLevel(String system, Integer level) throws CerberusException;

    List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystem(String system) throws CerberusException;

    public boolean insertBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant);

    public boolean deleteBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant);

    public boolean updateBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant);
}
