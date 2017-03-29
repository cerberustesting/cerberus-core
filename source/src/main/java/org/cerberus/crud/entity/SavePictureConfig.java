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

import org.cerberus.servlet.crud.test.PictureConnector;
import org.elfinder.servlets.config.AbstractConnectorConfig;
import org.elfinder.servlets.fs.DiskFsImpl;
import org.elfinder.servlets.fs.IFsImpl;

import java.io.File;

public class SavePictureConfig extends AbstractConnectorConfig {
    /**
     * Filesystem.
     */
    private DiskFsImpl fsImpl;

    public SavePictureConfig() {
        fsImpl = new DiskFsImpl();
    }

    @Override
    public IFsImpl getFs() {
        return fsImpl;
    }

    @Override
    public String getRoot() {
        return PictureConnector.HOME_SHARED_DOCS;
    }

    @Override
    public String getRootUrl() {
        return PictureConnector.REALOBJECTURL;
    }

    @Override
    public String getFileUrl(File path) {
        return getRootUrl() + "/" + getRelativePath(path);
    }

    @Override
    public String rootAliasOrBaseName() {
        return PictureConnector.SHARED_DOCS;
    }

    @Override
    public String getThumbnailUrl(File path) {
        return PictureConnector.THUMBNAIL + getRelativePath(path);
    }

    @Override
    public boolean hasThumbnail(File path) {
        String mimeType = getMime(path);
        return mimeType.contains("image");
    }
}
