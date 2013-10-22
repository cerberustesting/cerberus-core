package com.redcats.tst.servlet.documentation;

import com.redcats.tst.log.MyLogger;
import com.redcats.tst.service.IDocumentationService;
import org.apache.log4j.Level;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 * @version 1.0, 07/02/2013
 * @since 2.0.0
 */
@WebServlet(name = "DocumentationField", urlPatterns = {"/DocumentationField"})
public class DocumentationField extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        IDocumentationService docService = appContext.getBean(IDocumentationService.class);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        String result = "";

        String docTable = policy.sanitize(httpServletRequest.getParameter("docTable"));
        String docField = policy.sanitize(httpServletRequest.getParameter("docField"));
        String docLabel = policy.sanitize(httpServletRequest.getParameter("docLabel"));

        result = docService.findLabel(docTable, docField, docLabel);

        try {
            httpServletResponse.setContentType("text/html");
            httpServletResponse.getWriter().print(result);
        } catch (Exception exception) {
            MyLogger.log(DocumentationField.class.getName(), Level.WARN, exception.toString());
        }
    }
}
