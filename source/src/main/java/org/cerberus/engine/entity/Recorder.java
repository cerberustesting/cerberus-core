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
package org.cerberus.engine.entity;

/**
 *
 * @author bcivel
 */
public class Recorder {

    private String rootFolder;
    private String subFolder;
    private String subFolderURL;
    private String fullPath;
    private String fileName;
    private String fullFilename;
    private String relativeFilenameURL;
    private String level;

    /**
     * Root folder where the files will be stored. Defined from parameter.
     *
     * @return
     */
    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    /**
     * Calculated from execution ID in order to spread the files on different
     * folders. This is to prevent having too many objects on the rootFolder.
     *
     * @return
     */
    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
    }

    /**
     * Same as subFolder but with / as separator (will be used to store the path
     * in database).
     *
     * @return
     */
    public String getSubFolderURL() {
        return subFolderURL;
    }

    public void setSubFolderURL(String subFolderURL) {
        this.subFolderURL = subFolderURL;
    }

    /**
     * Full folder path where the file will be stored. Defined from RootFolder
     * and SubFolder
     *
     * @return
     */
    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    /**
     * Final filename.
     *
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Complete filename with system dependant File Separator. --> Used to save
     * the file on FileSystem.
     *
     * @return
     */
    public String getFullFilename() {
        return fullFilename;
    }

    public void setFullFilename(String fullFilename) {
        this.fullFilename = fullFilename;
    }

    /**
     * relative filename with / as a separator (from root folder). --> Saved
     * into database.
     *
     * @return
     */
    public String getRelativeFilenameURL() {
        return relativeFilenameURL;
    }

    public void setRelativeFilenameURL(String relativeFilenameURL) {
        this.relativeFilenameURL = relativeFilenameURL;
    }

    /**
     * Level where the file will be stored. That defined if the file is attached
     * to execution, action, control or property.
     *
     * @return
     */
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return this.fullFilename;
    }

}
