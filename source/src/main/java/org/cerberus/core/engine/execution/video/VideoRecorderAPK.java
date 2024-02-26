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

import com.google.common.collect.Lists;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.cerberus.core.crud.entity.Application;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.engine.entity.Recorder;
import org.cerberus.core.engine.entity.Session;
import org.cerberus.core.engine.execution.IRecorderService;

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

//    public void finalize() {
//        if (running.get())
//            endRecordVideo();
//    }

    @Override
    public void beginRecordVideo() {

        try {
            videoName = testCaseExecution.getTestCase() + "_" + new Date().getTime() + "_";

            running.set(true);
            threadRecorder = new Thread(() -> {
                try {
                    do {

                        String cmdtoRecord = "nohup screenrecord --bit-rate 5000000 --time-limit " + TIME_BY_VIDEO_SAMPLE + " /sdcard/" + videoName + cpt++ + ".mp4 >> /sdcard/logtotovideo.log 2>&1 &";

                        // execute screenrecord on background on mobile
                        executeCommand("echo '" + cmdtoRecord + "' > /sdcard/cmdtoRecord.sh");
                        executeCommand("ps | echo $(grep screenrecord) | cut -d \" \" -f2 | xargs kill -INT && sleep 1");
                        executeCommand("sh /sdcard/cmdtoRecord.sh > /dev/null 2>/dev/null &");


                        try { // wait video is terminated
                            Thread.sleep(TIME_BY_VIDEO_SAMPLE * 1000);
                        } catch (InterruptedException e) {
                            LOG.error("failed to sleep ...", e);
                        }
                    } while (running.get());
                }catch( Exception e) {
                    LOG.error("error during register video " + videoName + cpt + ".mp4", e);
                }

            });
            threadRecorder.start();


        } catch (Exception ex) {
            // log erreur, but don't fail ! Video is not a bloquant functionnality
            LOG.error("Failed to begin video : " + ex.toString(), ex);
        }

    }

    @Override
    public void endRecordVideo() {

        AndroidDriver driver = null;
        try {
            String applicationType = testCaseExecution.getAppTypeEngine();

            Session session = testCaseExecution.getSession();
            if (applicationType.equals(Application.TYPE_APK)) {

                running.set(false); // stop the thread

                // kill all screenrecord process from mobile
                executeCommand("ps | echo $(grep screenrecord) | cut -d \" \" -f2 | xargs kill -INT");

                String test = testCaseExecution.getTest();
                String testCase = testCaseExecution.getTestCase();
                driver = (AndroidDriver) session.getAppiumDriver();
        
                List<String> videosPath = new LinkedList<>();

                for (int i = 0; i < cpt; i++) {
                    Recorder recorder = recorderService.initFilenames(1l, test, testCase, null, null, null, null, "video-part", i, videoName + i, "mp4", false);

                    String videoStr = videoName + i + ".mp4";
                    String videoCompletePath = "/sdcard/" + videoStr;
                    String videoCompletePathTarget = recorder.getFullFilename();

                    try {
                        byte[] video = driver.pullFile(videoCompletePath);

                        FileUtils.writeByteArrayToFile(new File(videoCompletePathTarget), video);

                        // Index file created to database.
                        recorderService.addFileToTestCaseExecution(testCaseExecution, recorder, "Video", "MP4");

                        videosPath.add(videoCompletePathTarget);
                    } catch(Exception e) {
                        LOG.error("Failed to pull video on " + driver.getCapabilities().getCapability("deviceUDID") + " " + videoCompletePath);
                    }
                    finally {
                        // delete it from mobile
                        executeCommand("rm " + videoCompletePath);
                    }

                }

            }
        } catch (Exception ex) {
            // log erreur, but don't fail ! Video is not a bloquant functionnality
            LOG.error("Failed to end video on " +  (driver != null ? driver.getCapabilities().getCapability("deviceUDID") : "") + " : " + ex.toString(), ex);
        }

    }


    private void executeCommand(String cmd) throws IllegalArgumentException {
        AndroidDriver driver = (AndroidDriver) session.getAppiumDriver();

        Map<String, Object> argss = new HashMap<>();
        argss.put("command", cmd);
        argss.put("args", Lists.newArrayList(""));

        LOG.info(cmd + " : " + driver.executeScript("mobile: shell", argss).toString() + " on " + driver.getCapabilities().getCapability("deviceUDID"));
    }

}