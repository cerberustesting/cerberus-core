/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.BuildRevisionParameters;
import org.cerberus.core.crud.factory.IFactoryBuildRevisionParameters;
import org.springframework.stereotype.Service;


@Service
public class FactoryBuildRevisionParameters implements IFactoryBuildRevisionParameters {

    @Override
    public BuildRevisionParameters create(int id, String build, String revision, String release,
            String application, String project, String ticketIDFixed, String bugIDFixed, String link,
            String releaseOwner, String subject, Timestamp dateCre, String jenkinsBuildID,
            String mavenGroupID, String mavenArtefactID, String mavenVersion, String repositoryUrl) {
        BuildRevisionParameters newBuildRevisionParameters = new BuildRevisionParameters();
        newBuildRevisionParameters.setId(id);
        newBuildRevisionParameters.setBuild(build);
        newBuildRevisionParameters.setRevision(revision);
        newBuildRevisionParameters.setRelease(release);
        newBuildRevisionParameters.setApplication(application);
        newBuildRevisionParameters.setProject(project);
        newBuildRevisionParameters.setTicketIdFixed(ticketIDFixed);
        newBuildRevisionParameters.setBugIdFixed(bugIDFixed);
        newBuildRevisionParameters.setLink(link);
        newBuildRevisionParameters.setReleaseOwner(releaseOwner);
        newBuildRevisionParameters.setSubject(subject);
        newBuildRevisionParameters.setDatecre(dateCre);
        newBuildRevisionParameters.setJenkinsBuildId(jenkinsBuildID);
        newBuildRevisionParameters.setMavenGroupId(mavenGroupID);
        newBuildRevisionParameters.setMavenArtifactId(mavenArtefactID);
        newBuildRevisionParameters.setMavenVersion(mavenVersion);
        newBuildRevisionParameters.setRepositoryUrl(repositoryUrl);
        return newBuildRevisionParameters;
    }
    
}
