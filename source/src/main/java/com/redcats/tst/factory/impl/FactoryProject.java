/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.factory.impl;

import com.redcats.tst.entity.Project;
import com.redcats.tst.factory.IFactoryProject;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryProject implements IFactoryProject {

    @Override
    public Project create(String idProject, String code, String description, String active, String dateCreation) {
        Project project = new Project();
        project.setActive(active);
        project.setCode(code);
        project.setDateCreation(dateCreation);
        project.setDescription(description);
        project.setIdProject(idProject);
        return project;
    }

}
