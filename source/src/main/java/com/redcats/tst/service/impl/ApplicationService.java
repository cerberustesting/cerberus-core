/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service.impl;

import com.redcats.tst.dao.IApplicationDAO;
import com.redcats.tst.entity.Application;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.service.IApplicationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ApplicationService implements IApplicationService {

    @Autowired
    private IApplicationDAO ApplicationDAO;

    @Override
    public Application findApplicationByKey(String Application) throws CerberusException {
        return ApplicationDAO.findApplicationByKey(Application);
    }

    @Override
    public List<Application> findAllApplication() throws CerberusException {
        return ApplicationDAO.findAllApplication();
    }

    @Override
    public List<Application> findApplicationBySystem(String System) throws CerberusException {
        return ApplicationDAO.findApplicationBySystem(System);
    }

    @Override
    public boolean updateApplication(Application application) throws CerberusException {
        return ApplicationDAO.updateApplication(application);
    }

    @Override
    public boolean isApplicationExist(String Application) {
        try {
            findApplicationByKey(Application);
            return true;
        } catch (CerberusException e) {
            return false;
        }
    }

    @Override
    public List<String> findDistinctSystem() {
        return this.ApplicationDAO.findDistinctSystem();
    }
}
