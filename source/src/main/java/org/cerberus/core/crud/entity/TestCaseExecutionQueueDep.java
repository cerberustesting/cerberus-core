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
package org.cerberus.core.crud.entity;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class TestCaseExecutionQueueDep {

    private long id;
    private long exeQueueId;
    private String environment;
    private String country;
    private String tag;
    private String type;
    private String depTest;
    private String depTestCase;
    private Integer depTCDelay;
    private String depEvent;
    private Timestamp depDate;
    private String status;
    private Timestamp releaseDate;
    private String comment;
    private long exeId;
    private long queueId;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    /**
     * From here are data outside database model.
     */
    public static final String STATUS_WAITING = "WAITING"; // Dependency is still open and waiting.
    public static final String STATUS_IGNORED = "IGNORED"; // Dependency is closed and will not block any executions.
    public static final String STATUS_RELEASED = "RELEASED"; // Dependency has been released and no longuer block any executions.

    private static final Logger LOG = LogManager.getLogger(TestCaseExecutionQueueDep.class);

    /**
     * Convert the current TestCaseExecution into JSON format
     *
     * @return TestCaseExecution in JSONObject format
     */
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", this.getId());
            result.put("exeQueueId", this.getExeQueueId());
            result.put("comment", this.getComment());
            result.put("releaseDate", this.getReleaseDate());
            result.put("country", this.getCountry());
            result.put("dateCreated", this.getDateCreated());
            result.put("dateModif", this.getDateModif());
            result.put("depEvent", this.getDepEvent());
            result.put("depTest", this.getDepTest());
            result.put("depTestCase", this.getDepTestCase());
            result.put("depTCDelay", this.getDepTCDelay());
            result.put("depDate", this.getDepDate());
            result.put("environment", this.getEnvironment());
            result.put("exeId", this.getExeId());
            result.put("queueId", this.getQueueId());
            result.put("status", this.getStatus());
            result.put("tag", this.getTag());
            result.put("type", this.getType());
            result.put("usrCreated", this.getUsrCreated());
            result.put("usrModif", this.getUsrModif());

        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        } catch (Exception ex) {
            LOG.error(ex.toString(), ex);
        }
        return result;
    }

}
