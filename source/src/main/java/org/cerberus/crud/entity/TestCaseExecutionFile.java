/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

import java.sql.Timestamp;

/**
 * @author vertigo
 */
public class TestCaseExecutionFile {

    private long id;
    private long exeId;
    private String level;
    private String fileDesc;
    private String fileName;
    private String fileType;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExeId() {
        return exeId;
    }

    public void setExeId(long exeId) {
        this.exeId = exeId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFileDesc() {
        return fileDesc;
    }

    public void setFileDesc(String fileDesc) {
        this.fileDesc = fileDesc;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getUsrCreated() {
        return usrCreated;
    }

    public void setUsrCreated(String usrCreated) {
        this.usrCreated = usrCreated;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUsrModif() {
        return usrModif;
    }

    public void setUsrModif(String usrModif) {
        this.usrModif = usrModif;
    }

    public Timestamp getDateModif() {
        return dateModif;
    }

    public void setDateModif(Timestamp dateModif) {
        this.dateModif = dateModif;
    }

}
