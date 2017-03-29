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
package org.cerberus.servlet.manualtestcase;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.TestCaseStepActionExecution;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.log.MyLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 */
@WebServlet(name = "SaveManualExecutionPicture", urlPatterns = {"/SaveManualExecutionPicture"})
@MultipartConfig
public class SaveManualExecutionPicture extends HttpServlet {

    private final static Integer UPLOAD_PICTURE_MAXSIZE = 1048576;//1 MB

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //using commons-fileupload http://commons.apache.org/proper/commons-fileupload/using.html
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());

        //Collection<Part> parts = req.getParts(); //for sevlet 3.0, in glassfish 2.x does not work 
        TestCaseStepActionExecution tcsae = new TestCaseStepActionExecution();
        FileItem uploadedFile = null;

        //if is multipart we need to handle the upload data
        IParameterService parameterService = appContext.getBean(IParameterService.class);
        IRecorderService recorderService = appContext.getBean(IRecorderService.class);

        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        Integer imgPathMaxSize = parameterService.getParameterIntegerByKey("cerberus_screenshot_max_size", "", UPLOAD_PICTURE_MAXSIZE);
        
        // Set overall request size constraint
        upload.setFileSizeMax(imgPathMaxSize);//max size for the file
        try {
            // Parse the request
            List<FileItem> items = upload.parseRequest(req);

            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();

                if (item.isFormField()) {
                    tcsae = processFormField(tcsae, item);
                } else {
                    //uploadFileName = processUploadedFile(item);
                    uploadedFile = item;
                }
            }

            //this handles an action each time
        } catch (FileSizeLimitExceededException ex) {
            MyLogger.log(SaveManualExecutionPicture.class.getName(), Level.ERROR, "File size exceed the limit: " + ex.toString());
        } catch (FileUploadException ex) {
            MyLogger.log(SaveManualExecutionPicture.class.getName(), Level.ERROR, "Exception occurred while uploading file: " + ex.toString());
        }
        if (uploadedFile != null) {

            if (uploadedFile.getContentType().startsWith("image/")) {
                //TODO:FN verify if this is the best approach or if we should
                //check if the mime types for images can be configured in the web.xml and then obtain the valid mime types from the servletContext 
                //getServletContext().getMimeType(fileName);
                recorderService.recordUploadedFile(tcsae.getId(), tcsae, uploadedFile);
            } else {
                MyLogger.log(SaveManualExecutionPicture.class.getName(), Level.ERROR, "Problem with the file you're trying to upload. It is not an image."
                        + "Name: " + uploadedFile.getName() + "; Content-type: " + uploadedFile.getContentType());
            }
        }

        //old version : TODO to be deleted after testing
//        Collection<Part> parts = req.getParts();
//        String runId = req.getParameter("runId");
//        String test = req.getParameter("picTest");
//        String testCase = req.getParameter("picTestCase");
//        String step = req.getParameter("pictStep");
//        String action = req.getParameter("pictAction");
//        String control = req.getParameter("pictControl") == null ? "" : req.getParameter("pictControl");
//        String returnCode = req.getParameter("returnCode");
//
//        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
//        IParameterService parameterService = appContext.getBean(IParameterService.class);
////        ITestCaseStepExecutionService testCaseStepExecutionService = appContext.getBean(ITestCaseStepExecutionService.class);
////        ITestCaseStepActionExecutionService testCaseStepActionExecutionService = appContext.getBean(ITestCaseStepActionExecutionService.class);
//
//        try {
//            String imgPath = parameterService.findParameterByKey("cerberus_mediastorage_path", "").getValue();
//
//            File dir = new File(imgPath + runId);
//            dir.mkdirs();
//
//            int seq = 1;
//            long runID = ParameterParserUtil.parseLongParam(runId, 0);
//            //TestCaseStepActionExecution testCaseStepActionExecution = new TestCaseStepActionExecution();
//            for (Part p : parts) {
//                if (p.getName().equalsIgnoreCase("files[]")) {
//                    if (seq == 1) {
////                        TestCaseStepExecution tcse = new TestCaseStepExecution();
////                        tcse.setId(runID);
////                        tcse.setTest(test);
////                        tcse.setTestCase(testCase);
////                        tcse.setStep(1);
////                        testCaseStepExecutionService.insertTestCaseStepExecution(tcse);
//                    }
//
//                    InputStream inputStream = p.getInputStream();
//                    String controlName = control.equals("") ? "" : "Ct"+control;
//                    String name = test + "-" + testCase + "-St"+step+"Sq" + action + controlName + ".jpg";
//                    OutputStream outputStream = new FileOutputStream(new File(this.buildScreenshotPath(imgPath, runId, name)));
//
//                    int read;
//                    byte[] bytes = new byte[1024];
//                    while ((read = inputStream.read(bytes)) != -1) {
//                        outputStream.write(bytes, 0, read);
//                    }
//                    outputStream.close();
//
//                    Date now = new Date();
//
//                    //create action
////                    testCaseStepActionExecution.setId(runID);
////                    testCaseStepActionExecution.setTest(test);
////                    testCaseStepActionExecution.setTestCase(testCase);
////                    testCaseStepActionExecution.setStep(1);
////                    testCaseStepActionExecution.setSequence(seq);
////                    testCaseStepActionExecution.setReturnCode(returnCode);
////                    testCaseStepActionExecution.setReturnMessage("");
////                    testCaseStepActionExecution.setAction("screenshot");
////                    testCaseStepActionExecution.setObject("");
////                    testCaseStepActionExecution.setProperty("");
////                    testCaseStepActionExecution.setStart(now.getTime());
////                    testCaseStepActionExecution.setEnd(now.getTime());
////                    testCaseStepActionExecution.setStartLong(now.getTime());
////                    testCaseStepActionExecution.setEndLong(now.getTime());
////                    testCaseStepActionExecutionService.insertTestCaseStepActionExecution(testCaseStepActionExecution);
////
////                    seq++;
//                }
//            }
//
//        } catch (CerberusException e) {
//            MyLogger.log(SaveManualExecutionPicture.class.getName(), Level.ERROR, e.toString());
//        }
    }

    /**
     * Auxiliary function used to retrieve information about the test/test
     * case/step/action/control being executed.
     *
     * @param tcsae
     * @param name
     * @param value
     * @return
     */
    private TestCaseStepActionExecution extractInfoFromRequest(TestCaseStepActionExecution tcsae, String name, String value) {
        if (name.equals("pictStep")) {
            tcsae.setStep(Integer.valueOf(value));
        } else if (name.equals("picTest")) {
            tcsae.setTest(value);
        } else if (name.equals("picTestCase")) {
            tcsae.setTestCase(value);
        } else if (name.equals("runId")) {
            tcsae.setId(Long.valueOf(value));
        } else if (name.equals("pictAction")) {
            tcsae.setSequence(Integer.valueOf(value));
        }

        return tcsae;

    }

    private TestCaseStepActionExecution processFormField(TestCaseStepActionExecution tcsae, FileItem item) {
        String name = item.getFieldName();
        String value = item.getString();
        tcsae = extractInfoFromRequest(tcsae, name, value);
        return tcsae;
    }

}
