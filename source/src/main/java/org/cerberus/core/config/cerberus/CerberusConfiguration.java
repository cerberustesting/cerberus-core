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
package org.cerberus.core.config.cerberus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.concurrent.Executor;

/**
 * @author bcivel
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"org.cerberus"})
public class CerberusConfiguration {

    private static final Logger LOG = LogManager.getLogger(CerberusConfiguration.class);

    @Bean
    public DataSource dataSource() {

        String url      = System.getProperty("db.url");
        String username = System.getProperty("db.username");
        String password = System.getProperty("db.password");

        LOG.info("DataSource init → url={}, username={}", url, username);

        if (url == null) {
            throw new IllegalStateException("db.url system property is not set!");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("db.url"));
        config.setUsername(System.getProperty("db.username"));
        config.setPassword(System.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.getInteger("db.pool.maxTotal", 100));
        config.setMinimumIdle(Integer.getInteger("db.pool.maxIdle", 30));
        config.setConnectionTimeout(Long.getLong("db.pool.maxWaitMillis", 10000));
        config.setConnectionTestQuery("SELECT 1");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return new HikariDataSource(config);
    }

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
