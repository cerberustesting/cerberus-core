/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.refactor;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author ip100003
 */
public class importFile extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            try {
                List items = upload.parseRequest(request);
                Iterator iterator = items.iterator();
                File uploadedFile = null;
                String test = "";
                String testcase = "";
                String step = "";
                String load = "";
                while (iterator.hasNext()) {
                    FileItem item = (FileItem) iterator.next();

                    if (!item.isFormField()) {
                        String fileName = item.getName();

                        String root = getServletContext().getRealPath("/");
                        File pathFile = new File(root + "/cerberusFiles");
                        if (!pathFile.exists()) {
                            //boolean status = pathFile.mkdirs();
                            pathFile.mkdirs();
                        }

                        uploadedFile = new File(pathFile + "/" + fileName);
                        System.out.println(uploadedFile.getAbsolutePath());
                        item.write(uploadedFile);
                    } else {
                        String name = item.getFieldName();
                        if (name.equals("Test")) {
                            test = item.getString();
                        } else if (name.equals("Testcase")) {
                            testcase = item.getString();
                        } else if (name.equals("Step")) {
                            step = item.getString();
                        } else if (name.equals("Load")) {
                            load = item.getString();
                        }
                    }
                }
                response.sendRedirect("ImportHTML.jsp?Test=" + test + "&Testcase=" + testcase + "&Step=" + step + "&Load=" + load + "&FilePath=" + uploadedFile.getAbsolutePath());
            } catch (FileUploadException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
