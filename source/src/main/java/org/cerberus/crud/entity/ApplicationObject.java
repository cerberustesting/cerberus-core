/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.entity;

/**
 * @author vertigo
 */
public class ApplicationObject {

    private int ID;
    private String application;
    private String object;
    private String value;
    private String screenshotfilename;
    private String usrcreated;
    private String datecreated;
    private String usrmodif;
    private String datemodif;



    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getScreenShotFileName() {
        return screenshotfilename;
    }

    public void setScreenShotFileName(String screenshotfilename) {
        this.screenshotfilename = screenshotfilename;
    }

    public String getUsrCreated() { return usrcreated; }

    public void setUsrCreated(String usrcreated) {
        this.usrcreated = usrcreated;
    }

    public String getDateCreated() { return datecreated; }

    public void setDateCreated(String datecreated) {
        this.datecreated = datecreated;
    }

    public String getUsrModif() { return usrmodif; }

    public void setUsrModif(String usrmodif) {
        this.usrmodif = usrmodif;
    }

    public String getDateModif() { return datemodif; }

    public void setDateModif(String datemodif) {
        this.datemodif = datemodif;
    }

    @Override
    public String toString() {
        return application + " " + object;
    }
}
