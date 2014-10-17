package org.cerberus.servlet.manualTestCase;

import org.apache.log4j.Level;
import org.cerberus.entity.TestCaseStepActionExecution;
import org.cerberus.entity.TestCaseStepExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.IParameterService;
import org.cerberus.service.ITestCaseStepActionExecutionService;
import org.cerberus.service.ITestCaseStepExecutionService;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Collection;
import java.util.Date;

/**
 *
 */
@WebServlet(name = "SaveManualExecutionPicture", urlPatterns = {"/SaveManualExecutionPicture"})
@MultipartConfig
public class SaveManualExecutionPicture extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Collection<Part> parts = req.getParts();
        String runId = req.getParameter("runId");
        String test = req.getParameter("picTest");
        String testCase = req.getParameter("picTestCase");
        String returnCode = req.getParameter("returnCode");

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IParameterService parameterService = appContext.getBean(IParameterService.class);
//        ITestCaseStepExecutionService testCaseStepExecutionService = appContext.getBean(ITestCaseStepExecutionService.class);
//        ITestCaseStepActionExecutionService testCaseStepActionExecutionService = appContext.getBean(ITestCaseStepActionExecutionService.class);

        try {
            String imgPath = parameterService.findParameterByKey("cerberus_picture_path", "").getValue();

            File dir = new File(imgPath + runId);
            dir.mkdirs();

            int seq = 1;
            long runID = ParameterParserUtil.parseLongParam(runId, 0);
            //TestCaseStepActionExecution testCaseStepActionExecution = new TestCaseStepActionExecution();
            for (Part p : parts) {
                if (p.getName().equalsIgnoreCase("files[]")) {
                    if (seq == 1) {
//                        TestCaseStepExecution tcse = new TestCaseStepExecution();
//                        tcse.setId(runID);
//                        tcse.setTest(test);
//                        tcse.setTestCase(testCase);
//                        tcse.setStep(1);
//                        testCaseStepExecutionService.insertTestCaseStepExecution(tcse);
                    }

                    InputStream inputStream = p.getInputStream();
                    String name = test + "-" + testCase + "-St1Sq" + seq + ".jpg";
                    OutputStream outputStream = new FileOutputStream(new File(this.buildScreenshotPath(imgPath, runId, name)));

                    int read;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    outputStream.close();

                    Date now = new Date();

                    //create action
//                    testCaseStepActionExecution.setId(runID);
//                    testCaseStepActionExecution.setTest(test);
//                    testCaseStepActionExecution.setTestCase(testCase);
//                    testCaseStepActionExecution.setStep(1);
//                    testCaseStepActionExecution.setSequence(seq);
//                    testCaseStepActionExecution.setReturnCode(returnCode);
//                    testCaseStepActionExecution.setReturnMessage("");
//                    testCaseStepActionExecution.setAction("screenshot");
//                    testCaseStepActionExecution.setObject("");
//                    testCaseStepActionExecution.setProperty("");
//                    testCaseStepActionExecution.setStart(now.getTime());
//                    testCaseStepActionExecution.setEnd(now.getTime());
//                    testCaseStepActionExecution.setStartLong(now.getTime());
//                    testCaseStepActionExecution.setEndLong(now.getTime());
//                    testCaseStepActionExecution.setScreenshotFilename(name);
//                    testCaseStepActionExecutionService.insertTestCaseStepActionExecution(testCaseStepActionExecution);
//
//                    seq++;
                }
            }

        } catch (CerberusException e) {
            MyLogger.log(SaveManualExecutionPicture.class.getName(), Level.ERROR, e.toString());
        }
    }

    private String buildScreenshotPath(String folder, String runId, String name){
        return folder + runId + "/" + name;
    }
}
