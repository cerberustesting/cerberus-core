package org.cerberus.crud.entity;

import org.cerberus.servlet.testcase.PictureConnector;
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
