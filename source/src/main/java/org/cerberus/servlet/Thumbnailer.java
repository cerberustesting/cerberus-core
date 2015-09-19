package org.cerberus.servlet;

import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleOp;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.cerberus.servlet.testcase.SavePicture;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

@WebServlet(name = "thumbnailer", urlPatterns = {"/thumbnailer"})
public class Thumbnailer extends HttpServlet{
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int width = (!StringUtils.isEmpty(request.getParameter("w"))) ? Integer.valueOf(request.getParameter("w")) : 150;

        String path = request.getParameter("p");

        // security
        path = FilenameUtils.normalize(path);
        File f = new File(SavePicture.HOME_SHARED_DOCS, path);

        Boolean real = request.getParameter("r") != null;
        BufferedImage b = null;


        if (real) {
            b = ImageIO.read(f);
            ImageIO.write(b, "png", response.getOutputStream());
            return;
        }

        BufferedImage image = ImageIO.read(f);

        ResampleOp rop = new ResampleOp(DimensionConstrain.createMaxDimension(width, -1));
        rop.setNumberOfThreads(4);
        b = rop.filter(image, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(b, "png", baos);
//        byte[] bytesOut = baos.toByteArray();

        response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());
        response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2 * 360).toGMTString());

        ImageIO.write(b, "png", response.getOutputStream());
    }
}
