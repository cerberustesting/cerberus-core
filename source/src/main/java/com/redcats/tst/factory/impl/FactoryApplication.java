/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Application;
import com.redcats.tst.factory.IFactoryApplication;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryApplication implements IFactoryApplication {

    @Override
    public Application create(String application, String description, String internal
            , int sort, String type, String system, String subsystem
            , String svnurl, String deploytype, String mavengroupid
            , String bugTrackerUrl, String bugTrackerNewUrl) {
        Application newApplication = new Application();
        newApplication.setApplication(application);
        newApplication.setDeploytype(deploytype);
        newApplication.setDescription(description);
        newApplication.setInternal(internal);
        newApplication.setMavengroupid(mavengroupid);
        newApplication.setSort(sort);
        newApplication.setSubsystem(subsystem);
        newApplication.setSvnurl(svnurl);
        newApplication.setSystem(system);
        newApplication.setType(type);
        newApplication.setBugTrackerUrl(bugTrackerUrl);
        newApplication.setBugTrackerNewUrl(bugTrackerNewUrl);
        return newApplication;
    }


}
