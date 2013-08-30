package com.redcats.tst.dao;

import com.redcats.tst.entity.DeployType;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * @author bdumont
 */
public interface IDeployTypeDAO {

    DeployType findDeployTypeByKey(String deploytype) throws CerberusException;

    List<DeployType> findAllDeployType();
}
