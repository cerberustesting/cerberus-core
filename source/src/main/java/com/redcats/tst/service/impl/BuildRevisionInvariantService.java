/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IBuildRevisionInvariantDAO;
import com.redcats.tst.entity.BuildRevisionInvariant;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IBuildRevisionInvariantService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BuildRevisionInvariantService implements IBuildRevisionInvariantService {

    @Autowired
    private IBuildRevisionInvariantDAO BuildRevisionInvariantDAO;

    @Override
    public BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, Integer seq) throws CerberusException {
        return BuildRevisionInvariantDAO.findBuildRevisionInvariantByKey(system, level, seq);
    }

    @Override
    public BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, String versionName) throws CerberusException {
        return BuildRevisionInvariantDAO.findBuildRevisionInvariantByKey(system, level, versionName);
    }

    @Override
    public List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystemLevel(String system, Integer level) throws CerberusException {
        return BuildRevisionInvariantDAO.findAllBuildRevisionInvariantBySystemLevel(system, level);
    }

    @Override
    public List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystem(String system) throws CerberusException {
        return BuildRevisionInvariantDAO.findAllBuildRevisionInvariantBySystem(system);
    }

    @Override
    public boolean insertBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.insertBuildRevisionInvariant(buildRevisionInvariant);
    }

    @Override
    public boolean deleteBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.deleteBuildRevisionInvariant(buildRevisionInvariant);
    }

    @Override
    public boolean updateBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        return BuildRevisionInvariantDAO.updateBuildRevisionInvariant(buildRevisionInvariant);
    }
    
}
