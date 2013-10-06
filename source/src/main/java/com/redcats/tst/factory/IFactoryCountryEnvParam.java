/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory;

import com.redcats.tst.entity.CountryEnvParam;

/**
 *
 * @author bcivel
 */
public interface IFactoryCountryEnvParam {

    CountryEnvParam create(String system, String country, String environment, String build, String revision, String chain,
            String distribList, String eMailBodyRevision, String type, String eMailBodyChain,
            String eMailBodyDisableEnvironment, boolean active, boolean maintenanceAct, String maintenanceStr, String maintenanceEnd);
}
