package org.cerberus.filter;


import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class SimpleCORSFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;

        String allowOrigin = this.getEnv("FRONT_URL");

        if(! StringUtils.isEmpty(allowOrigin) ) {
            response.setHeader("Access-Control-Allow-Origin", allowOrigin);

            response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        chain.doFilter(req, resp);
    }

    private String getEnv(String value) {
        String res = System.getProperty(value);

        if(StringUtils.isEmpty(res ) )
            res = System.getenv(value);

        return res;
    }

}

