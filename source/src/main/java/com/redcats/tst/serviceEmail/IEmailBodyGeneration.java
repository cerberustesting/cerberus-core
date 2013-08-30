/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.serviceEmail;

import java.sql.Connection;

/**
 *
 * @author bcivel
 */
public interface IEmailBodyGeneration {

    public String GenerateBuildContentTable(String build, String revision, String lastBuild, String lastRevision, Connection conn) ;

    public String GenerateTestRecapTable(String build, String revision, String country, Connection conn) ;
    
}
