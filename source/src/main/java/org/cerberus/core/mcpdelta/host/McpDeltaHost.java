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
package org.cerberus.core.mcpdelta.host;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.api.services.DebugExecutionService;
import org.cerberus.core.api.services.QueuedExecutionService;
import org.cerberus.core.crud.entity.LogEvent;
import org.cerberus.core.crud.entity.Parameter;
import org.cerberus.core.crud.service.ILogEventService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.crud.service.ITagSystemService;
import org.cerberus.core.engine.queuemanagement.IExecutionThreadPoolService;
import org.cerberus.core.mcpdelta.Context;
import org.cerberus.core.mcpdelta.McpEndpoint;
import org.cerberus.core.mcpdelta.db.Db;
import org.cerberus.core.service.xray.IXRayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * MCP Delta hosted by the Cerberus webapp: built on Cerberus' connection pool and parameters, reaching the queue
 * and the debug mode through Cerberus' services, logging its calls like the MCP endpoint does. Built on first
 * use, once the webapp is up.
 *
 * <p>JVM properties (all optional): {@code org.cerberus.mcpdelta.home} (journal folder, default
 * {@code <catalina.base>/mcpdelta}, never a folder the web serves), {@code org.cerberus.mcpdelta.grid} (Selenium grid for live page
 * outlines; default: the first active robot executor without credentials), {@code org.cerberus.mcpdelta.allowedOrigins}
 * (comma-separated browser origins allowed besides the instance's own).</p>
 */
@Component
public class McpDeltaHost {

    private static final Logger LOG = LogManager.getLogger(McpDeltaHost.class);

    @Autowired
    private DataSource dataSource;
    @Autowired
    private IParameterService parameterService;
    @Autowired
    private ILogEventService logEventService;
    @Autowired
    private QueuedExecutionService queuedExecutionService;
    @Autowired
    private DebugExecutionService debugExecutionService;
    @Autowired
    private IXRayService xrayService;
    @Autowired
    private ITagSystemService tagSystemService;
    @Autowired
    private IExecutionThreadPoolService executionThreadPoolService;

    private volatile McpEndpoint endpoint;
    private volatile Context context;

    public McpEndpoint endpoint() {
        McpEndpoint e = endpoint;
        if (e == null) {
            synchronized (this) {
                if (endpoint == null) {
                    context = new Context(config(), new Db(dataSource), new InProcessGateway(queuedExecutionService, debugExecutionService,
                            parameterService, xrayService, tagSystemService, executionThreadPoolService));
                    McpEndpoint built = new McpEndpoint(context);
                    // Same audit trail as the MCP endpoint (cerberus_log_mcpcalls): who called which tool, never the arguments.
                    built.onCall(r -> logEventService.createForMcpCalls("delta/" + r.tool(), "CALL", r.error() ? LogEvent.STATUS_WARN : LogEvent.STATUS_INFO,
                            "MCP Delta " + r.tool() + " · " + (r.error() ? "error" : "ok") + " · " + r.millis() + " ms · args " + r.argChars()
                                    + " chars · answer " + r.resultChars() + " chars", r.user()));
                    LOG.info("MCP Delta {} ready on /mcpdelta/mcp ({} tools, journal {})", McpEndpoint.VERSION, built.toolCount(), context.config.home);
                    endpoint = built;
                }
                e = endpoint;
            }
        }
        return e;
    }

    /** Delta's settings, from Cerberus parameters and JVM properties instead of environment variables. */
    private Context.Config config() {
        String media = parameterService.getParameterStringByKey(Parameter.VALUE_cerberus_exeautomedia_path, "", "/opt/CerberusMedias/executions/");
        // The journal holds the exact rows before each write (undo), secrets included: never under a folder the
        // web can serve. Tomcat's own base folder, outside webapps/, survives redeployments.
        String home = System.getProperty("org.cerberus.mcpdelta.home",
                Path.of(System.getProperty("catalina.base", System.getProperty("user.home")), "mcpdelta").toString());
        String grid = System.getProperty("org.cerberus.mcpdelta.grid", "");
        return new Context.Config(key -> switch (key) {
            case "DELTA_MEDIA_DIR" -> media;
            case "DELTA_HOME" -> home;
            case "DELTA_GRID_URL" -> grid; // empty: the first active robot executor without credentials
            case "DELTA_USER" -> "MCPDelta";
            default -> null;
        });
    }

    /**
     * The caller's Cerberus roles: those its authentication carries (HTTP Basic or a Keycloak token), plus those the
     * Cerberus user table grants its login (an API key carries none).
     */
    public Set<String> rolesOf(String login, Collection<? extends GrantedAuthority> authorities) {
        Set<String> roles = new LinkedHashSet<>();
        if (authorities != null) {
            for (GrantedAuthority a : authorities) {
                String name = a.getAuthority();
                roles.add(name != null && name.startsWith("ROLE_") ? name.substring(5) : name);
            }
        }
        endpoint();
        try {
            roles.addAll(context.db.read(c -> Db.query(c, "SELECT Role FROM userrole WHERE Login=?", login)).stream().map(r -> r.s("Role")).toList());
        } catch (RuntimeException e) {
            LOG.warn("MCP Delta could not read the roles of {}: {}", login, e.getMessage());
        }
        return roles;
    }
}
