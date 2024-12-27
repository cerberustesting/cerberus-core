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
package org.cerberus.core.engine.execution.video;

import java.util.concurrent.atomic.AtomicBoolean;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCaseExecution;

import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.engine.execution.IRecorderService;

/**
 * Class to record Video
 *
 * To day only available with APK
 *
 * How to use ?
 *
 *
 * VideoRecorder videoRecorder = VideoRecorder.getInstance(tCExecution,
 * recorderService); // can throw an UnsupportedOperationException .... try {
 * .... videoRecorder.beginRecordVideo() ... } finally {
 * videoRecorder.endRecordVideo() }
 *
 * If you forgot to `endRecordVideo`, `VideoRecorder` implements a destructor
 * who end video when object is destructed
 */
public abstract class VideoRecorder {

    protected final IRecorderService recorderService;
    protected TestCaseExecution testCaseExecution;
    protected Session session;
    protected AtomicBoolean running = new AtomicBoolean(false);
    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(VideoRecorder.class);

    protected static final int TIME_BY_VIDEO_SAMPLE = 20; // max 180 for screenrecord

    /**
     * Create a new instance of VideoRecorder
     *
     * @param testCaseExecution
     * @param recorderService
     * @return 
     * @throws UnsupportedOperationException if application type is not
     * supported (only supported by APK at this time)
     */
    public static VideoRecorder getInstance(TestCaseExecution testCaseExecution, IRecorderService recorderService) {
        String applicationType = null;
        if (testCaseExecution != null && testCaseExecution.getApplicationObj() != null) {
            applicationType = testCaseExecution.getAppTypeEngine();
        }

        if (Application.TYPE_APK.equals(applicationType)) {
            return new VideoRecorderAPK(testCaseExecution, recorderService);
        }

        throw new UnsupportedOperationException("Application type '" + applicationType + "' is not supported by Video");
    }

    protected VideoRecorder(TestCaseExecution testCaseExecution, IRecorderService recorderService) {
        this.testCaseExecution = testCaseExecution;
        this.session = testCaseExecution.getSession();
        this.recorderService = recorderService;
    }

//    @Override
//    public void finalize() {
//        if (running.get()) {
//            endRecordVideo();
//        }
//    }

    public abstract void beginRecordVideo();

    public abstract void endRecordVideo();
}
