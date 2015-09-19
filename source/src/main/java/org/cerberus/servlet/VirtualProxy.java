package org.cerberus.servlet;

import org.apache.commons.io.FilenameUtils;
import org.cerberus.servlet.testcase.SavePicture;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@WebServlet(name = "virtualproxy", urlPatterns = {"/virtualproxy "})
public class VirtualProxy extends HttpServlet{
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getParameter("p");

// security
        path = FilenameUtils.normalize(path);

        File file = new File(SavePicture.HOME_SHARED_DOCS, path);


        FileInputStream fileIn = new FileInputStream(file);
        ServletOutputStream out = response.getOutputStream();

        byte[] outputByte = new byte[4096];
        while (fileIn.read(outputByte, 0, 4096) != -1) {
            out.write(outputByte, 0, 4096);
        }
        fileIn.close();
        out.flush();
        out.close();
    }
}
