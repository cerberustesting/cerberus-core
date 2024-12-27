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
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author vertigo17
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class EventHook {

    private Integer id;
    private String eventReference;
    private String objectKey1;
    private String objectKey2;
    private boolean isActive;
    private String hookConnector;
    private String hookRecipient;
    private String hookChannel;
    private String description;
    @EqualsAndHashCode.Exclude
    private String usrCreated;
    @EqualsAndHashCode.Exclude
    private Timestamp dateCreated;
    @EqualsAndHashCode.Exclude
    private String usrModif;
    @EqualsAndHashCode.Exclude
    private Timestamp dateModif;
    // External Database model

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String HOOKCONNECTOR_EMAIL = "EMAIL";
    public static final String HOOKCONNECTOR_SLACK = "SLACK";
    public static final String HOOKCONNECTOR_TEAMS = "TEAMS";
    public static final String HOOKCONNECTOR_GOOGLECHAT = "GOOGLE-CHAT";
    public static final String HOOKCONNECTOR_GENERIC = "GENERIC";

    /**
     * Invariant PROPERTY TYPE String.
     */
    public static final String EVENTREFERENCE_CAMPAIGN_START = "CAMPAIGN_START";
    public static final String EVENTREFERENCE_CAMPAIGN_END = "CAMPAIGN_END";
    public static final String EVENTREFERENCE_CAMPAIGN_END_CIKO = "CAMPAIGN_END_CIKO";

    public static final String EVENTREFERENCE_EXECUTION_START = "EXECUTION_START";
    public static final String EVENTREFERENCE_EXECUTION_END = "EXECUTION_END";
    public static final String EVENTREFERENCE_EXECUTION_END_LASTRETRY = "EXECUTION_END_LASTRETRY";

    public static final String EVENTREFERENCE_TESTCASE_CREATE = "TESTCASE_CREATE";
    public static final String EVENTREFERENCE_TESTCASE_UPDATE = "TESTCASE_UPDATE";
    public static final String EVENTREFERENCE_TESTCASE_DELETE = "TESTCASE_DELETE";

    public static final String EVENTREFERENCE_ENVIRONMENT_DISABLE = "ENVIRONMENT_DISABLE";
    public static final String EVENTREFERENCE_ENVIRONMENT_REVISIONCHANGE = "ENVIRONMENT_REVISIONCHANGE";
    
    private static final Logger LOG = LogManager.getLogger(EventHook.class);

    public boolean hasSameKey(EventHook obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        if (!Objects.equals(getId(), obj.getId())) {
            return false;
        }

        return true;
    }


    public JSONObject toJson() {
        JSONObject labelJson = new JSONObject();
        try {
            labelJson.put("id", this.getId());
            labelJson.put("eventReference", this.getEventReference());
            labelJson.put("objectKey1", this.getObjectKey1());
            labelJson.put("objectKey2", this.getObjectKey2());
            labelJson.put("isActive", this.isActive());
            labelJson.put("hookConnector", this.getHookConnector());
            labelJson.put("hookRecipient", this.getHookRecipient());
            labelJson.put("hookChannel", this.getHookChannel());
            labelJson.put("description", this.getDescription());
            labelJson.put("usrCreated", this.getUsrCreated());
            labelJson.put("dateCreated", this.getDateCreated());
            labelJson.put("usrModif", this.getUsrModif());
            labelJson.put("dateModif", this.getDateModif());
        } catch (JSONException ex) {
            LOG.error(ex.toString(), ex);
        }
        return labelJson;
    }

}
