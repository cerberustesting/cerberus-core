/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.Application;

/**
 * @author vertigo
 */
public interface IFactoryApplication {

    /**
     * @param application  ID of the application.
     * @param description  Description of the Application.
     * @param internal
     * @param sort
     * @param type
     * @param system
     * @param subsystem
     * @param svnurl
     * @param deploytype
     * @param mavengroupid
     * @return
     */
    Application create(String application, String description, String internal
            , int sort, String type, String system, String subsystem
            , String svnurl, String deploytype, String mavengroupid
            , String bugtrackerurl, String bugtrackernewurl);


}
