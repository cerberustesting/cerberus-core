/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.Application;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author vertigo
 */
public interface IApplicationService {

    /**
     *
     * @param Application
     * @return Application object with all properties feeded.
     * @throws CerberusException if Application not found.
     */
    Application findApplicationByKey(String Application) throws CerberusException;

    /**
     *
     * @return the list of all Applications.
     * @throws CerberusException when no application exist.
     */
    List<Application> findAllApplication() throws CerberusException;

    /**
     *
     * @return the list of all Applications.
     * @throws CerberusException when no application exist.
     */
    List<Application> findApplicationBySystem(String System) throws CerberusException;

    /**
     *
     * @return boolean.
     * @throws CerberusException when no application exist.
     */
    boolean updateApplication(Application application) throws CerberusException;

    /**
     *
     * @param Application
     * @return true is application exist or false is application does not exist
     * in database.
     */
    boolean isApplicationExist(String Application);
}
