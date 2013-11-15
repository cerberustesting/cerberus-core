/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.refactor;

public class Test {

    private String application;
    private String description;
    private String name;
    private String priority;
    private String status;
    private String test;
    private String testcase;
    private String comment;
    private String behaviororvalueexpected;
    private String bugid;
    private String group;
    private String targetBuild;
    private String targetRev;

    public String getBehaviorOrValueExpected() {
        return behaviororvalueexpected;
    }

    public void setBehaviorOrValueExpected(String behaviororvalueexpected) {
        this.behaviororvalueexpected = behaviororvalueexpected;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBugID() {
        return bugid;
    }

    public void setBugID(String bugid) {
        this.bugid = bugid;
    }

    public String getTargetBuild() {
        return targetBuild;
    }

    public void setTargetBuild(String targetBuild) {
        this.targetBuild = targetBuild;
    }

    public String getTargetRev() {
        return targetRev;
    }

    public void setTargetRev(String targetRev) {
        this.targetRev = targetRev;
    }

    public String getApplication() {

        return this.application;
    }

    public String getDescription() {

        return this.description;
    }

    public String getName() {

        return this.name;
    }

    public String getPriority() {

        return this.priority;
    }

    public String getStatus() {

        return this.status;
    }

    public String getGroup() {

        return this.group;
    }

    public String getTest() {

        return this.test;
    }

    public String getTestcase() {

        return this.testcase;
    }

    public void setApplication(String application) {

        this.application = application;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setPriority(String priority) {

        this.priority = priority;
    }

    public void setGroup(String group) {

        this.group = group;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public void setTest(String test) {

        this.test = test;
    }

    public void setTestcase(String testcase) {

        this.testcase = testcase;
    }
}
