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
package org.cerberus.core.crud.entity;

import java.sql.Timestamp;

public class BuildRevisionParameters {

    private int id;
    private String build;
    private String revision;
    private String release;
    private String application;
    private String project;
    private String ticketIdFixed;
    private String bugIdFixed;
    private String link;
    private String releaseOwner;
    private String subject;
    private Timestamp datecre;
    private String jenkinsBuildId;
    private String mavenGroupId;
    private String mavenArtifactId;
    private String mavenVersion;
    private String repositoryUrl;

    /**
     * From here are data outside database model.
     */
    private String appDeployType;

    public String getAppDeployType() {
        return appDeployType;
    }

    public void setAppDeployType(String appDeployType) {
        this.appDeployType = appDeployType;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTicketIdFixed() {
        return ticketIdFixed;
    }

    public void setTicketIdFixed(String ticketIdFixed) {
        this.ticketIdFixed = ticketIdFixed;
    }

    public String getBugIdFixed() {
        return bugIdFixed;
    }

    public void setBugIdFixed(String bugIdFixed) {
        this.bugIdFixed = bugIdFixed;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getReleaseOwner() {
        return releaseOwner;
    }

    public void setReleaseOwner(String releaseOwner) {
        this.releaseOwner = releaseOwner;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Timestamp getDatecre() {
        return datecre;
    }

    public void setDatecre(Timestamp datecre) {
        this.datecre = datecre;
    }

    public String getJenkinsBuildId() {
        return jenkinsBuildId;
    }

    public void setJenkinsBuildId(String jenkinsBuildId) {
        this.jenkinsBuildId = jenkinsBuildId;
    }

    public String getMavenGroupId() {
        return mavenGroupId;
    }

    public void setMavenGroupId(String mavenGroupId) {
        this.mavenGroupId = mavenGroupId;
    }

    public String getMavenArtifactId() {
        return mavenArtifactId;
    }

    public void setMavenArtifactId(String mavenArtifactId) {
        this.mavenArtifactId = mavenArtifactId;
    }

    public String getMavenVersion() {
        return mavenVersion;
    }

    public void setMavenVersion(String mavenVersion) {
        this.mavenVersion = mavenVersion;
    }
}
