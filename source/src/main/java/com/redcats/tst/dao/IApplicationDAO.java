package com.redcats.tst.dao;

import com.redcats.tst.entity.Application;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 31/12/2012
 * @since 2.0.0
 */
public interface IApplicationDAO {

    /**
     *
     * @param application ID of the Application to find.
     * @return the application if exist.
     * @throws a CerberusException when Application does not exist.
     */
    Application findApplicationByKey(String application) throws CerberusException;

    /**
     *
     * @return a list of all application.
     * @throws CerberusException when no application exist.
     */
    public List<Application> findAllApplication() throws CerberusException;

    /**
     *
     * @return a list of all application.
     * @throws CerberusException when no application exist.
     */
    public List<Application> findApplicationBySystem(String System) throws CerberusException;

    /**
     *
     * @return a list of all application.
     * @throws CerberusException when no application exist.
     */
    public boolean updateApplication(Application application) throws CerberusException;

}
