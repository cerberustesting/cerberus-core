package org.cerberus.engine.execution.video;



import java.util.concurrent.atomic.AtomicBoolean;

import org.cerberus.crud.entity.*;
import org.cerberus.engine.entity.Session;
import org.cerberus.engine.execution.IRecorderService;

/**
 * Class to record Video
 *
 * To day only available with APK
 *
 * How to use ?
 *
 *
 * VideoRecorder videoRecorder = VideoRecorder.getInstance(tCExecution, recorderService); // can throw an UnsupportedOperationException
 * ....
 * try {
 *      ....
 *      videoRecorder.beginRecordVideo()
 *      ...
 * } finally {
 *     videoRecorder.endRecordVideo()
 * }
 *
 * If you forgot to `endRecordVideo`, `VideoRecorder` implements a destructor who end video when object is detructed
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
     * @throws UnsupportedOperationException  if application type is not supported (only supported by APK at this time)
     */
    public static VideoRecorder getInstance(TestCaseExecution testCaseExecution, IRecorderService recorderService) {
        String applicationType = null;
        if(testCaseExecution !=null && testCaseExecution.getApplicationObj() != null)
            applicationType = testCaseExecution.getApplicationObj().getType();

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

    @Override
    public void finalize() {
        if (running.get())
            endRecordVideo();
    }

    public abstract void beginRecordVideo();

    public abstract void endRecordVideo();
}