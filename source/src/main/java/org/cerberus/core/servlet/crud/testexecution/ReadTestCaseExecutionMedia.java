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
package org.cerberus.core.servlet.crud.testexecution;

import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionFile;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITestCaseExecutionFileService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.ParameterParserUtil;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerList;
import org.cerberus.core.util.servlet.ServletUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author vertigo
 */
@WebServlet(name = "ReadTestCaseExecutionMedia", urlPatterns = {"/ReadTestCaseExecutionMedia"})
public class ReadTestCaseExecutionMedia extends HttpServlet {

    private static final Logger LOG = LogManager.getLogger(ReadTestCaseExecutionMedia.class);

    private IFactoryTestCaseExecutionFile factoryTestCaseExecutionFile;

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
        String charset = request.getCharacterEncoding() == null ? "UTF-8" : request.getCharacterEncoding();
        boolean auto = ParameterParserUtil.parseBooleanParam(request.getParameter("auto"), true);
        String type = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("type"), "", charset);
        String test = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("test"), "", charset);
        String testcase = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("testcase"), "", charset);
        String fileName = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("filename"), "", charset);
        String fileType = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("filetype"), "", charset);
        String fileDesc = ParameterParserUtil.parseStringParamAndDecodeAndSanitize(request.getParameter("filedesc"), "", charset);
        int stepId = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("step"), 0, charset);
        int index = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("index"), 1, charset);
        int sequence = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("sequence"), 0, charset);
        int sequenceControl = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("sequenceControl"), 0, charset);
        int iterator = ParameterParserUtil.parseIntegerParamAndDecode(request.getParameter("iterator"), 0, charset);
        boolean autoContentType = ParameterParserUtil.parseBooleanParam(request.getParameter("autoContentType"), true);
        long id = ParameterParserUtil.parseLongParamAndDecode(request.getParameter("id"), 0, charset);

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IParameterService parameterService = appContext.getBean(IParameterService.class);

        BufferedImage b = null;

        AnswerList al = new AnswerList<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));

        TestCaseExecutionFile tceFile = null;
        if (!(fileName.isEmpty())) {
            // Secure that you cannot access unnecessary folders
            fileName = fileName.replace("..", ".");
            IFactoryTestCaseExecutionFile factoryTestCaseExecutionFile = appContext.getBean(IFactoryTestCaseExecutionFile.class);
            tceFile = factoryTestCaseExecutionFile.create(0, 0, "", fileDesc, fileName, fileType, "", null, "", null);

        } else {

            ITestCaseExecutionFileService testCaseExecutionFileService = appContext.getBean(ITestCaseExecutionFileService.class);

            String levelFile = "";
            if (type.equals("action")) {
                levelFile = test + "-" + testcase + "-" + stepId + "-" + index + "-" + sequence;
            } else if (type.equals("control")) {
                levelFile = test + "-" + testcase + "-" + stepId + "-" + index + "-" + sequence + "-" + sequenceControl;
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
            } else {
                // If previous read failed we try without index. (that can be removed few moths after stepId index has been introduced in Jan 2017)
                if (type.equals("action")) {
                    levelFile = test + "-" + testcase + "-" + stepId + "-" + sequence;
                } else if (type.equals("control")) {
                    levelFile = test + "-" + testcase + "-" + stepId + "-" + sequence + "-" + sequenceControl;
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
        }

        if (tceFile != null) {
            String pathString = "";
            if (auto) {
                pathString = parameterService.getParameterStringByKey("cerberus_exeautomedia_path", "", "");
            } else {
                pathString = parameterService.getParameterStringByKey("cerberus_exemanualmedia_path", "", "");
            }
            int nb = tceFile.getFileName().split("/").length;
            String filenameDownload = "";
            if (nb > 1) {
                filenameDownload = String.valueOf(tceFile.getFileName().split("/")[nb - 2]) + "-" + tceFile.getFileName().split("/")[nb - 1];
            } else if (nb > 0) {
                filenameDownload = tceFile.getFileName().split("/")[nb - 1];
            } else {
                filenameDownload = tceFile.getFileName();
            }

            switch (tceFile.getFileType()) {
                case "JPG":
                case "JPEG":
                    if (autoContentType) {
                        response.setContentType("image/jpeg");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnImage(request, response, tceFile, pathString);
                    break;
                case "PNG":
                    if (autoContentType) {
                        response.setContentType("image/png");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnImage(request, response, tceFile, pathString);
                    break;
                case "GIF":
                    if (autoContentType) {
                        response.setContentType("image/gif");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnImage(request, response, tceFile, pathString);
                    break;
                case "XML":
                    if (autoContentType) {
                        response.setContentType("application/xml");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnFile(request, response, tceFile, pathString);
                    break;
                case "JSON":
                    if (autoContentType) {
                        response.setContentType("application/json");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnFile(request, response, tceFile, pathString);
                    break;
                case "TXT":
                    if (autoContentType) {
                        response.setContentType("application/txt");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnFile(request, response, tceFile, pathString);
                    break;
                case "PDF":
                    if (autoContentType) {
                        response.setContentType("application/pdf");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnPDF(request, response, tceFile, pathString);
                    break;
                case "HTML":
                    if (autoContentType) {
                        response.setContentType("text/html");
                    }
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnDownloadTXTFile(request, response, tceFile, pathString);
                    break;
                case "BIN":
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnDownloadBinFile(request, response, tceFile, pathString);
                    break;
                case "MP4":
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
                    returnMP4(request, response, tceFile, pathString);
                    break;
                default:
                    response.setHeader("Content-Disposition", "filename=" + filenameDownload);
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

        File picture = new File(filePath + tc.getFileName());
        LOG.debug("Accessing File : " + picture.getAbsolutePath());
        try {
            if (real) {
                b = ImageIO.read(picture);
                ImageIO.write(b, "png", response.getOutputStream());
            } else {
                image = ImageIO.read(picture);

                // We test if file is too thin or too long. That prevent 500 error in case files are not compatible with resize. In that case, we crop the file.
                if ((image.getHeight() * width / image.getWidth() < 10) || (image.getWidth() * height / image.getHeight() < 15)) {
                    LOG.debug("Image is too big of thin. Target Height : " + image.getHeight() * width / image.getWidth() + " Target Width : " + image.getWidth() * height / image.getHeight());
                    b = ImageIO.read(picture);
                    int minwidth = width;
                    if (width > image.getWidth()) {
                        minwidth = image.getWidth();
                    }
                    int minheight = height;
                    if (height > image.getHeight()) {
                        minheight = image.getHeight();
                    }
                    BufferedImage crop = b.getSubimage(0, 0, minwidth, minheight);

                    b = crop;
                    response.setHeader("Format-Status", "ERROR");
                    response.setHeader("Format-Status-Message", "Image Crop from : " + image.getWidth() + "X" + image.getHeight() + " to : " + minwidth + "X" + minheight);
                } else {
                    ResampleOp rop = new ResampleOp(DimensionConstrain.createMaxDimension(width, height, true));
                    rop.setNumberOfThreads(4);
                    b = rop.filter(image, null);
                    response.setHeader("Format-Status", "OK");
                }
            }
        } catch (IOException e) {

        }

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        sdf.applyPattern("dd MMM yyyy HH:mm:ss z");

        response.setHeader("Last-Modified", sdf.format(DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360)));
        response.setDateHeader("Expires", 0);
        response.setHeader("Type", "PNG");
        response.setHeader("Description", tc.getFileDesc());

        ImageIO.write(b, "png", response.getOutputStream());
    }

    private void returnPDF(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {

        File pdfFile = null;
        filePath = StringUtil.addSuffixIfNotAlready(filePath, File.separator);
        pdfFile = new File(filePath + tc.getFileName());
        response.setContentType("application/pdf");
        response.setContentLength((int) pdfFile.length());
        try {
            FileUtils.copyFile(pdfFile, response.getOutputStream());
        } catch (IOException e) {
            LOG.warn(e);
        }

    }

    private void returnMP4(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {

        File mp4File = null;
        filePath = StringUtil.addSuffixIfNotAlready(filePath, File.separator);
        mp4File = new File(filePath + tc.getFileName());
        response.setContentType("video/mp4");
        response.setContentLength((int) mp4File.length());
        response.setHeader("Content-Range", "bytes start-end/length");
        try {
            FileUtils.copyFile(mp4File, response.getOutputStream());
        } catch (IOException e) {
            LOG.warn(e);
        }

    }

    private void returnFile(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
        response.setHeader("Last-Modified", sdf.format(DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360)));
        response.setDateHeader("Expires", 0);
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());

        filePath = StringUtil.addSuffixIfNotAlready(filePath, File.separator);
        File file = new File(filePath + tc.getFileName());
        LOG.debug("Accessing File : " + filePath + tc.getFileName());
        try {
            FileUtils.copyFile(file, response.getOutputStream());
        } catch (FileNotFoundException e) {
            LOG.warn(e);
        } catch (IOException e) {
            LOG.warn(e);
        }

    }

    private void returnDownloadTXTFile(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
        response.setHeader("Last-Modified", sdf.format(DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360)));
        response.setDateHeader("Expires", 0);
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());

        filePath = StringUtil.addSuffixIfNotAlready(filePath, File.separator);
        File file = new File(filePath + tc.getFileName());
        LOG.debug("Accessing File : " + filePath + tc.getFileName());
        try {
            FileUtils.copyFile(file, response.getOutputStream());
        } catch (FileNotFoundException e) {
            LOG.warn(e);
        } catch (IOException e) {
            LOG.warn(e);
        }

    }

    private void returnDownloadBinFile(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {
        LOG.debug("Accessing File : " + filePath + tc.getFileName());

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
        response.setHeader("Last-Modified", sdf.format(DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360)));
        response.setDateHeader("Expires", 0);
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());

        filePath = StringUtil.addSuffixIfNotAlready(filePath, File.separator);
        File file = new File(filePath + tc.getFileName());
        LOG.debug("Accessing File : " + filePath + tc.getFileName());
        try {
            FileUtils.copyFile(file, response.getOutputStream());
        } catch (FileNotFoundException e) {
            LOG.error(e, e);
        } catch (IOException e) {
            LOG.error(e, e);
        }

    }

    private void returnText(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        sdf.applyPattern("dd MMM yyyy HH:mm:ss z");

        response.setHeader("Last-Modified", sdf.format(DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360)));
        response.setDateHeader("Expires", 0);
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());
    }

    private void returnNotSupported(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, String filePath) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        sdf.applyPattern("dd MMM yyyy HH:mm:ss z");

        response.setHeader("Last-Modified", sdf.format(DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360)));
        response.setHeader("Expires", sdf.format(DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360)));
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
            LOG.warn(ex);
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
            LOG.warn(ex);
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
