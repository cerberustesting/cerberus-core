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

import com.google.gson.Gson;
import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleOp;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.service.*;
import org.cerberus.crud.service.impl.*;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.entity.Session;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.servlet.crud.test.PictureConnector;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import sun.misc.BASE64Decoder;

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

        AnswerItem a = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        AnswerList al = new AnswerList<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));

        String levelFile = "";
        if(type.equals("action")){
            levelFile =  test + "-" + testcase + "-" + step + "-" + sequence;
        }else if(type.equals("control")){
            levelFile =  test + "-" + testcase + "-" + step + "-" + sequence  + "-" + sequenceControl;
        }
        al = testCaseExecutionFileService.readByVarious(id,levelFile);
        TestCaseExecutionFile tceFile = null;
        Parameter path = null;
        if(al.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && !al.getDataList().isEmpty()){
            Iterator i = al.getDataList().iterator();
            a = parameterService.readByKey("","cerberus_mediastorage_path");
            if(a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                int index = -1;
                while(i.hasNext() && index != iterator){
                    index++;
                    TestCaseExecutionFile tctemp = (TestCaseExecutionFile) i.next();
                    if(index == iterator) {
                        tceFile = tctemp;
                        path = (Parameter) a.getItem();
                    }
                }
            }
        }
        if(tceFile != null && path != null){
            if(isFileAnImage(tceFile)){
                returnImage(request, response, tceFile, path);
            }else if(isFileAXML(tceFile)){
                returnXML(request, response, tceFile, path);
            }else if(isFileAText(tceFile)){
                returnText(request, response, tceFile, path);
            }else{
                returnNotSupported(request, response, tceFile, path);
            }
        }
    }

    private boolean isFileAnImage(TestCaseExecutionFile tc){
        String extension = tc.getFileType();
        return "JPG".equals(extension) || "PNG".equals(extension) || "GIF".equals(extension) || "JPEG".equals(extension);
    }

    private boolean isFileAXML(TestCaseExecutionFile tc){
        String extension = tc.getFileType();
        return "HTML".equals(extension) || "XML".equals(extension);
    }

    private boolean isFileAText(TestCaseExecutionFile tc){
        String extension = tc.getFileType();
        return "TEXT".equals(extension);
    }

    private void returnImage(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, Parameter p) throws IOException {

        int width = (!StringUtils.isEmpty(request.getParameter("w"))) ? Integer.valueOf(request.getParameter("w")) : 150;
        int height = (!StringUtils.isEmpty(request.getParameter("h"))) ? Integer.valueOf(request.getParameter("h")) : 100;

        Boolean real = request.getParameter("r") != null;

        BufferedImage image = null;
        BufferedImage b = null;
        String uploadPath = p.getValue();
        uploadPath = StringUtil.addSuffixIfNotAlready(uploadPath, "/");

        File picture = new File(uploadPath + tc.getFileName());
        try {
            if (real) {
                b = ImageIO.read(picture);
                ImageIO.write(b, "png", response.getOutputStream());
            }else {
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

    private void returnXML(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, Parameter p){

        String everything = "";
        String uploadPath = p.getValue();
        uploadPath = StringUtil.addSuffixIfNotAlready(uploadPath, "/");

        try(FileInputStream inputStream = new FileInputStream(uploadPath + tc.getFileName())) {
            everything = IOUtils.toString(inputStream);
            response.getWriter().print(everything);
        }catch(FileNotFoundException e){

        }catch(IOException e){

        }


        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());
    }

    private void returnText(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, Parameter p){
        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Type", tc.getFileType());
        response.setHeader("Description", tc.getFileDesc());
    }

    private void returnNotSupported(HttpServletRequest request, HttpServletResponse response, TestCaseExecutionFile tc, Parameter p){
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
