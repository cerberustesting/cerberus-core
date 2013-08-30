/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Environment;

/**
 *
 * @author bcivel
 */
public interface IFactoryEnvironment {
    
    Environment create( String env, String ip,String url,String urlLogin,String build,String revision,
    boolean active,String typeApplication,String seleniumIp,String seleniumPort,String seleniumBrowser,
    String path,boolean maintenance,String maintenanceStr,String maintenanceEnd);
}
