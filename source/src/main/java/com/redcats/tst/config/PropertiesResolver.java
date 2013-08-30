package com.redcats.tst.config;

import com.redcats.tst.log.MyLogger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Level;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 11/03/2013
 * @since 2.0.0
 */
public class PropertiesResolver extends PropertyPlaceholderConfigurer {

    public PropertiesResolver() {
        super();
    }

    @Override
    protected void loadProperties(Properties props) throws IOException {
        String env = System.getProperty("org.cerberus.environment");

        if (env == null) {
            MyLogger.log(PropertiesResolver.class.getName(), Level.FATAL,
                    "Environment Property (org.cerberus.environment) not defined. Please, refer to the README file to configure it");
        }

        props.setProperty("org.cerberus.environment", env);
    }
}
