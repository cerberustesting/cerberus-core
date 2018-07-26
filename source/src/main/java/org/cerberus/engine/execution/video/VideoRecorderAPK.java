package org.cerberus.engine.execution.video;


import com.google.common.collect.Lists;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.cerberus.crud.entity.Application;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.engine.entity.Recorder;
import org.cerberus.engine.entity.Session;
import org.cerberus.engine.execution.IRecorderService;

import java.io.File;
import java.util.*;

/**
 * Class to record video on apk
 */
public class VideoRecorderAPK extends VideoRecorder {
    private int cpt = 0;
    private String videoName = null;
    private Thread threadRecorder = null;
    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(VideoRecorderAPK.class);

    VideoRecorderAPK(TestCaseExecution testCaseExecution, IRecorderService recorderService) {
       super(testCaseExecution,recorderService);
    }

    public void finalize() {
        if (running.get())
            endRecordVideo();
    }

    public void beginRecordVideo() {

        try {
            videoName = testCaseExecution.getTestCase() + "_" + new Date().getTime() + "_";

            running.set(true);
            threadRecorder = new Thread(() -> {
                // todo decouper par 20s et réassembler la vidéo,

                do {

                    String cmdtoRecord = "nohup screenrecord --bit-rate 5000000 --time-limit " + TIME_BY_VIDEO_SAMPLE + " /sdcard/" + videoName + cpt++ + ".mp4 &";

                    // execute screenrecord on background on mobile
                    executeCommand("echo '" + cmdtoRecord + "' > /sdcard/cmdtoRecord.sh");
                    executeCommand("sh /sdcard/cmdtoRecord.sh > /dev/null 2>/dev/null &");


                    try { // wait video is terminated
                        Thread.sleep(TIME_BY_VIDEO_SAMPLE * 1000);
                    } catch (InterruptedException e) {
                        LOG.error("failed to sleep ...");
                    }
                } while (running.get());
            });
            threadRecorder.start();


        } catch (Exception ex) {
            // log erreur, but don't fail ! Video is not a bloquant functionnality
            LOG.error("Failed to begin video : " + ex.toString(), ex);
        }

    }

    public void endRecordVideo() {

        try {
            String applicationType = testCaseExecution.getApplicationObj().getType();

            Session session = testCaseExecution.getSession();
            if (applicationType.equals(Application.TYPE_APK)) {

                running.set(false); // stop the thread

                // kill all screenrecord process from mobile
                executeCommand("ps | echo $(grep screenrecord) | cut -d \" \" -f2 | xargs kill -INT");

                String test = testCaseExecution.getTest();
                String testCase = testCaseExecution.getTestCase();
                AppiumDriver driver = session.getAppiumDriver();

                List<String> videosPath = new LinkedList<>();

                for (int i = 0; i < cpt; i++) {

                    Recorder recorder = recorderService.initFilenames(1l, test, testCase, null, null, null, null, "video-part", i, videoName + i, "mp4", false);

                    String videoStr = videoName + i + ".mp4";
                    String videoCompletePath = "/sdcard/" + videoStr;
                    String videoCompletePathTarget = recorder.getFullFilename();

                    byte[] video = driver.pullFile(videoCompletePath);

                    FileUtils.writeByteArrayToFile(new File(videoCompletePathTarget), video);

                    // Index file created to database.
                    recorderService.addFileToTestCaseExecution(testCaseExecution, recorder, "Video", "MP4");

                    videosPath.add(videoCompletePathTarget);

                    // delete it from mobile
                    executeCommand("rm " + videoCompletePath);

                }

            }
        } catch (Exception ex) {
            // log erreur, but don't fail ! Video is not a bloquant functionnality
            LOG.error("Failed to end video : " + ex.toString(), ex);
        }

    }


    private void executeCommand(String cmd) throws IllegalArgumentException {
        AndroidDriver driver = ((AndroidDriver) session.getAppiumDriver());

        Map<String, Object> argss = new HashMap<>();
        argss.put("command", cmd);
        argss.put("args", Lists.newArrayList(""));

        LOG.info(cmd + " : " + driver.executeScript("mobile: shell", argss).toString());
    }

}