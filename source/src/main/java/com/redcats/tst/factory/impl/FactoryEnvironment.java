/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Environment;
import com.redcats.tst.factory.IFactoryEnvironment;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryEnvironment implements IFactoryEnvironment {

    @Override
    public Environment create(String env, String ip, String url, String urlLogin, String build, String revision, boolean active,
                              String typeApplication, String seleniumIp, String seleniumPort, String seleniumBrowser, String path,
                              boolean maintenance, String maintenanceStr, String maintenanceEnd) {

        Environment newEnvironment = new Environment();
        newEnvironment.setEnv(env);
        newEnvironment.setIp(ip);
        newEnvironment.setUrl(url);
        newEnvironment.setUrlLogin(urlLogin);
        newEnvironment.setBuild(build);
        newEnvironment.setRevision(revision);
        newEnvironment.setActive(active);
        newEnvironment.setTypeApplication(typeApplication);
        newEnvironment.setSeleniumIp(seleniumIp);
        newEnvironment.setSeleniumPort(seleniumPort);
        newEnvironment.setSeleniumBrowser(seleniumBrowser);
        newEnvironment.setPath(path);
        newEnvironment.setMaintenance(maintenance);
        newEnvironment.setMaintenanceStr(maintenanceStr);
        newEnvironment.setMaintenanceEnd(maintenanceEnd);

        return newEnvironment;
    }

}
