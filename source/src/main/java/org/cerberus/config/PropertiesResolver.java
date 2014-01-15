/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

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
