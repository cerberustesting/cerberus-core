/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.servlet.crud.testexecution;

import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleOp;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.service.*;
import org.cerberus.crud.service.impl.*;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author vertigo
 */
@WebServlet(name = "ReadTestCaseExecutionImage", urlPatterns = {"/ReadTestCaseExecutionImage"})
public class ReadTestCaseExecutionImage extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ReadTestCaseExecutionImage.class);

    private IApplicationObjectService applicationObjectService;

    String data = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNiYAAAAAkAAxkR2eQAAAAASUVORK5CYII=";
    String base64Image = data.split(",")[1];
    byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException {
        String charset = request.getCharacterEncoding();

        String type = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("type"), "", charset);
        String test = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), "", charset);
        String testcase = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testcase"), "", charset);
        int step = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("step"), 0, charset);
        int index = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("index"), 1, charset);
        int sequence = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("sequence"), 0, charset);
        int sequenceControl = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("sequenceControl"), 0, charset);
        int iterator = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("iterator"), 0, charset);
        long id = ParameterParserUtil.parseLongParamAndDecode(request.getParameter("id"), 0, charset);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(TestCaseExecutionService.class);
        ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService = appContext.getBean(TestCaseStepActionControlExecutionService.class);
        ITestCaseStepActionExecutionService testCaseStepActionExecutionService = appContext.getBean(TestCaseStepActionExecutionService.class);
        ITestCaseExecutionFileService testCaseExecutionFileService = appContext.getBean(TestCaseExecutionFileService.class);
        IParameterService parameterService = appContext.getBean(ParameterService.class);

        BufferedImage b = null;

        AnswerList al = new AnswerList<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));

        String levelFile = "";
        if (type.equals("action")) {
            levelFile = test + "-" + testcase + "-" + step + "-" + index + "-" + sequence;
        } else if (type.equals("control")) {
            levelFile = test + "-" + testcase + "-" + step + "-" + index + "-" + sequence + "-" + sequenceControl;
        }
        al = testCaseExecutionFileService.readByVarious(id, levelFile);
        TestCaseExecutionFile tceFile = null;
        if (al.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && !al.getDataList().isEmpty()) {
            Iterator i = al.getDataList().iterator();
            int indexIterator = -1;
            while (i.hasNext() && indexIterator != iterator) {
                indexIterator++;
                TestCaseExecutionFile tctemp = (TestCaseExecutionFile) i.next();
                if (indexIterator == iterator) {
                    tceFile = tctemp;
                }
            }
        } else {
            // If previous read failed we try without index. (that can be removed few moths after step index has been introduced in Jan 2017)
            if (type.equals("action")) {
                levelFile = test + "-" + testcase + "-" + step + "-" + sequence;
            } else if (type.equals("control")) {
                levelFile = test + "-" + testcase + "-" + step + "-" + sequence + "-" + sequenceControl;
            }
            al = testCaseExecutionFileService.readByVarious(id, levelFile);
            if (al.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && !al.getDataList().isEmpty()) {
                Iterator i = al.getDataList().iterator();
                int indexIterator = -1;
                while (i.hasNext() && indexIterator != iterator) {
                    indexIterator++;
                    TestCaseExecutionFile tctemp = (TestCaseExecutionFile) i.next();
                    if (indexIterator == iterator) {
                        tceFile = tctemp;
                    }
                }
            }
        }

        if (tceFile != null) {
            String pathString = parameterService.getParameterStringByKey("cerberus_mediastorage_path", "", "");
            switch (tceFile.getFileType()) {
                case "JPG":
                case "PNG":
                case "GIF":
                case "JPEG":
                    returnImage(request, response, tceFile, pathString);
                    break;
                case "HTML":
                case "XML":
                    returnXML(request, response, tceFile, pathString);
                    break;
                case "TEXT":
                    returnText(request, response, tceFile, pathString);
                    break;
                default:
                    returnNotSupported(request, response, tceFile, pathString);
            }

        }
    }

    private void returnImage(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) throws IOException {

        int width = (!StringUtils.isEmpty(request.getParameter("w"))) ? Integer.valueOf(request.getParameter("w")) : 150;
        int height = (!StringUtils.isEmpty(request.getParameter("h"))) ? Integer.valueOf(request.getParameter("h")) : 100;

        Boolean real = request.getParameter("r") != null;

        BufferedImage image = null;
        BufferedImage b = null;
        filePath = StringUtil.addSuffixIfNotAlready(filePath, File.separator);
        LOG.debug("TOTO File Path : " + filePath);

        File picture = new File(filePath + tc.getFileName());
        try {
            if (real) {
                b = ImageIO.read(picture);
                ImageIO.write(b, "png", response.getOutputStream());
            } else {
                image = ImageIO.read(picture);

                ResampleOp rop = new ResampleOp(DimensionConstrain.createMaxDimension(width, height, true));
                rop.setNumberOfThreads(4);
                b = rop.filter(image, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(b, "png", baos);
            }
        } catch (IOException e) {

        }

        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Type", "PNG");
        response.setHeader("Description", tc.getFileDesc());

        ImageIO.write(b, "png", response.getOutputStream());
    }

    private void returnXML(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {

        String everything = "";
        filePath = StringUtil.addSuffixIfNotAlready(filePath, "/");

        try (FileInputStream inputStream = new FileInputStream(filePath + tc.getFileName())) {
            everything = IOUtils.toString(inputStream);
            response.getWriter().print(everything);
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());
    }

    private void returnText(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {
        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());
    }

    private void returnNotSupported(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {
        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Type", tc.getFileType());
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(ReadTestCaseExecutionImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            Logger.getLogger(ReadTestCaseExecutionImage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
