/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao;

import com.redcats.tst.entity.Project;
import java.util.List;

/**
 *
 * @author bcivel
 */
public interface IProjectDAO {
    
List<Project> findAllProject();
            
}
