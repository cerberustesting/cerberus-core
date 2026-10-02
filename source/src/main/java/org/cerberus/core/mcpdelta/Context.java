/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.mcpdelta;

import org.cerberus.core.mcpdelta.catalog.Catalog;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.mcpdelta.journal.Journal;

import java.nio.file.Path;

/** Everything a tool needs, wired once at start-up. */
public final class Context {

    public final Config config;
    public final Db db;
    public final Catalog catalog;
    public final Journal journal;
    public final org.cerberus.core.mcpdelta.cerberus.CerberusGateway gateway;

    /** The standalone server: its own JDBC connection and Cerberus over HTTP. */
    public Context(Config config) {
        this(config, new Db(config.dbUrl, config.dbUser, config.dbPassword), new org.cerberus.core.mcpdelta.cerberus.HttpGateway(config));
    }

    /** Inside a host (the Cerberus webapp): its connection pool and its own way of reaching Cerberus. */
    public Context(Config config, Db db, org.cerberus.core.mcpdelta.cerberus.CerberusGateway gateway) {
        this.config = config;
        this.db = db;
        this.catalog = new Catalog(db);
        this.journal = new Journal(Path.of(config.home, "journal"));
        this.gateway = gateway;
    }

    /**
     * The Selenium grid for live page outlines: the configured one, else the first active robot executor of the
     * instance that needs no credentials.
     */
    /**
     * The Selenium grids that can open a page: the configured one, else every distinct grid of the active robot
     * executors without credentials, chrome robots first. Some may be down: the caller tries them in turn.
     */
    public java.util.List<String> grids() {
        if (!config.gridUrl.isBlank()) {
            return java.util.List.of(config.gridUrl);
        }
        java.util.List<Db.Row> rows = db.read(c -> Db.query(c, "SELECT x.host, x.port FROM robotexecutor x JOIN robot b ON b.robot=x.robot"
                + " WHERE x.IsActive=1 AND b.IsActive=1 AND COALESCE(x.HostUser,'')='' AND COALESCE(x.host,'')<>''"
                + " ORDER BY COALESCE(b.browser,'')<>'chrome', x.`rank`, x.robot"));
        java.util.Set<String> grids = new java.util.LinkedHashSet<>();
        for (Db.Row r : rows) {
            String host = r.s("host").trim().contains("://") ? r.s("host").trim() : "http://" + r.s("host").trim();
            grids.add(host + (r.s("port").isBlank() || host.matches(".*:\\d+/?$") ? "" : ":" + r.s("port").trim()));
        }
        if (grids.isEmpty()) {
            throw new org.cerberus.core.mcpdelta.tools.Tool.ToolError("no Selenium grid to open the page: none is configured and no active robot executor"
                    + " without credentials exists");
        }
        return new java.util.ArrayList<>(grids);
    }

    /** Who the current call writes as: the authenticated Cerberus login, else the configured user. */
    public String user() {
        String u = org.cerberus.core.mcpdelta.util.CallContext.user();
        return u != null ? u : config.user;
    }

    /** Settings: environment variables with local defaults (standalone), or values given by the host. */
    public static final class Config {
        private final java.util.function.Function<String, String> source;

        public Config() {
            this(System::getenv);
        }

        /** @param source DELTA_* key → value, or null for the default */
        public Config(java.util.function.Function<String, String> source) {
            this.source = source;
            this.dbUrl = env("DELTA_DB_URL",
                "jdbc:mysql://localhost:3306/cerberus?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
                        + "&tinyInt1isBit=false&zeroDateTimeBehavior=CONVERT_TO_NULL");
            this.dbUser = env("DELTA_DB_USER", "cerberus");
            this.dbPassword = env("DELTA_DB_PASSWORD", "toto");
            this.cerberusUrl = env("DELTA_CERBERUS_URL", "http://localhost:8080");
            this.cerberusApiKey = env("DELTA_CERBERUS_APIKEY", "");
            this.cerberusLogin = env("DELTA_CERBERUS_LOGIN", "cerberus");
            this.cerberusPassword = env("DELTA_CERBERUS_PASSWORD", "cerberus");
            this.gridUrl = env("DELTA_GRID_URL", "http://localhost:4444");
            this.mediaDir = env("DELTA_MEDIA_DIR",
                System.getProperty("user.home") + "/Ceberus/cerberus-core/source/localdata/cerberusmedia/executions");
            this.home = env("DELTA_HOME", System.getProperty("user.home") + "/.mcpdelta");
            this.user = env("DELTA_USER", "MCPDelta");
            this.bind = env("DELTA_BIND", "127.0.0.1");
            this.port = Integer.parseInt(env("DELTA_PORT", "8091"));
            this.maxAutoTestcases = Integer.parseInt(env("DELTA_MAX_AUTO", "25"));
            this.maxWaitSeconds = Integer.parseInt(env("DELTA_MAX_WAIT", "45"));
            this.readBudget = Integer.parseInt(env("DELTA_READ_BUDGET", "6000"));
        }

        public final String dbUrl;
        public final String dbUser;
        public final String dbPassword;
        public final String cerberusUrl;
        public final String cerberusApiKey;
        /** Cerberus account used for the debug mode API when no API key is set (local default instance account). */
        public final String cerberusLogin;
        public final String cerberusPassword;
        /** Selenium grid reachable from MCP Delta, for live page outlines (read live:, debug mode). */
        public final String gridUrl;
        public final String mediaDir;
        public final String home;
        public final String user;
        public final String bind;
        public final int port;
        /** Above this many testcases, or for any deletion, a write is planned and waits for an explicit apply. */
        public final int maxAutoTestcases;
        /** Longest a run call waits: MCP clients cut a call after about a minute. */
        public final int maxWaitSeconds;
        /** Default reading budget, in tokens (4 characters each). */
        public final int readBudget;

        private String env(String key, String def) {
            String v = source.apply(key);
            return v == null ? def : v.trim();
        }
    }
}
