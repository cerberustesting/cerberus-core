package com.redcats.tst.dao;

import com.redcats.tst.entity.BuildRevisionInvariant;
import com.redcats.tst.exception.CerberusException;

import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface IBuildRevisionInvariantDAO {

    /**
     *
     * @param system
     * @param level
     * @param seq
     * @return the BuildRevisionInvariant that correspond to the system, level
     * and sequence.
     * @throws CerberusException in case the BuildRevisionInvariant is not
     * found.
     */
    BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, Integer seq) throws CerberusException;

    /**
     * @param system
     * @param level
     * @param versionName
     * @return the BuildRevisionInvariant that correspond to the system, level
     * and versionName.
     * @throws CerberusException in case the BuildRevisionInvariant is not
     * found.
     */
    BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, String versionName) throws CerberusException;

    /**
     * @param system
     * @param level
     * @return a list of all BuildRevisionInvariant that correspond to the
     * system and level.
     * @throws CerberusException in case no user can be found.
     */
    List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystemLevel(String system, Integer level) throws CerberusException;

    /**
     * @param system
     * @return a list of all BuildRevisionInvariant that correspond to the
     * system and level.
     * @throws CerberusException in case no user can be found.
     */
    List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystem(String system) throws CerberusException;

    /**
     * Insert user into the database.
     *
     * @param buildRevisionInvariant
     * @return true if insert BuildRevisionInvariant successful
     * @throws CerberusException if we did not manage to insert the
     * BuildRevisionInvariant.
     */
    public boolean insertBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant);

    /**
     * delete user from the database.
     *
     * @param BuildRevisionInvariant
     * @return true if delete BuildRevisionInvariant successful
     * @throws CerberusException if BuildRevisionInvariant could not be removed.
     */
    public boolean deleteBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant);

    /**
     * update user that correspond to the user.getUserID.
     *
     * @param BuildRevisionInvariant
     * @return true if update BuildRevisionInvariant successful
     * @throws CerberusException if the BuildRevisionInvariant could not be
     * updated.
     */
    public boolean updateBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant);
}
