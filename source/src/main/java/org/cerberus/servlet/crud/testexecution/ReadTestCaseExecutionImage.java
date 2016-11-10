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
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.cerberus.crud.entity.*;
import org.cerberus.crud.service.*;
import org.cerberus.crud.service.impl.*;
import org.cerberus.engine.entity.MessageEvent;
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
        long id = ParameterParserUtil.parseLongParamAndDecode(request.getParameter("id"), 0, charset);


        int width = (!StringUtils.isEmpty(request.getParameter("w"))) ? Integer.valueOf(request.getParameter("w")) : 150;
        int height = (!StringUtils.isEmpty(request.getParameter("h"))) ? Integer.valueOf(request.getParameter("h")) : 100;

        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        ITestCaseExecutionService testCaseExecutionService = appContext.getBean(TestCaseExecutionService.class);
        ITestCaseStepActionControlExecutionService testCaseStepActionControlExecutionService = appContext.getBean(TestCaseStepActionControlExecutionService.class);
        ITestCaseStepActionExecutionService testCaseStepActionExecutionService = appContext.getBean(TestCaseStepActionExecutionService.class);
        ITestCaseExecutionFileService testCaseExecutionFileService = appContext.getBean(TestCaseExecutionFileService.class);
        IParameterService parameterService = appContext.getBean(ParameterService.class);

        BufferedImage b ;
        // create a buffered image
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        b = ImageIO.read(bis);
        bis.close();

        AnswerItem a = new AnswerItem(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        String levelFile = "";
        if(type.equals("action")){
            levelFile =  test + "-" + testcase + "-" + step + "-" + sequence;
        }else if(type.equals("control")){
            levelFile =  test + "-" + testcase + "-" + step + "-" + sequence  + "-" + sequenceControl;
        }
        AnswerList al = new AnswerList<>(new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED));
        al = testCaseExecutionFileService.readByVarious(id,levelFile);
        BufferedImage image = null;

        if(al.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && !al.getDataList().isEmpty()){
            Iterator i = al.getDataList().iterator();
            while(i.hasNext()){
                TestCaseExecutionFile tc = (TestCaseExecutionFile)i.next();
                MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                        "cerberus_mediastorage_path Parameter not found");
                a = parameterService.readByKey("","cerberus_mediastorage_path");
                if(a.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())){
                    Parameter p = (Parameter) a.getItem();
                    String uploadPath = p.getValue();
                    uploadPath = StringUtil.addSuffixIfNotAlready(uploadPath, "/");
                    File picture = new File(uploadPath + tc.getFileName());
                    try {
                        image = ImageIO.read(picture);
                    } catch (IOException e) {

                    }
                }
                String file = tc.getFileName();
                ResampleOp rop = new ResampleOp(DimensionConstrain.createMaxDimension(width, height, true));
                rop.setNumberOfThreads(4);
                b = rop.filter(image, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(b, "png", baos);
                //        byte[] bytesOut = baos.toByteArray();
            }
        }
        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());

        ImageIO.write(b, "png", response.getOutputStream());
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
