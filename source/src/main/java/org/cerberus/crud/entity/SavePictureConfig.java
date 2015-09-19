package org.cerberus.crud.entity;

import org.cerberus.servlet.testcase.SavePicture;
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
        return SavePicture.HOME_SHARED_DOCS;
    }

    @Override
    public String getRootUrl() {
        return SavePicture.REALOBJECTURL;
    }

    @Override
    public String getFileUrl(File path) {
        return getRootUrl() + "/" + getRelativePath(path);
    }

    @Override
    public String rootAliasOrBaseName() {
        return SavePicture.SHARED_DOCS;
    }

    @Override
    public String getThumbnailUrl(File path) {
        return SavePicture.THUMBNAIL + getRelativePath(path);
    }

    @Override
    public boolean hasThumbnail(File path) {
        String mimeType = getMime(path);
        return mimeType.contains("image");
    }
}
