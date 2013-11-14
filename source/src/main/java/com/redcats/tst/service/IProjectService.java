/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.service;

import com.redcats.tst.entity.Project;
import com.redcats.tst.exception.CerberusException;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface IProjectService {
  
    /**
     * @param project
     * @return the project object
     * @throws CerberusException if not exist.
     */
    Project findProjectByKey(String project) throws CerberusException;

    List<String> findListOfProjectDescription();

    /**
     *
     * @param project
     * @return true if project exist. false if not.
     */
    boolean isProjectExist(String project);
    
    
}
