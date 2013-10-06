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
public interface IEmailGeneration {

    public String EmailGenerationRevisionChange(String system, String country, String env, String build, String revision, Connection conn);

    public String EmailGenerationDisableEnv(String system, String country, String env);

    public String EmailGenerationNewChain(String system, String country, String env, String build, String revision, String chain);
}
