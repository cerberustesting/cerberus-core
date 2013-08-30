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

    public String EmailGenerationRevisionChange(String country, String env, String build, String revision, Connection conn);

    public String EmailGenerationDisableEnv(String country, String env);

    public String EmailGenerationNewChain(String country, String env, String build, String revision, String chain);
}
