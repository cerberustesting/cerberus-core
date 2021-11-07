/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.entity;

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
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
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

    private static final Logger LOG = LogManager.getLogger(EventHook.class);

//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getEventReference() {
//        return eventReference;
//    }
//
//    public void setEventReference(String eventReference) {
//        this.eventReference = eventReference;
//    }
//
//    public String getObjectKey1() {
//        return objectKey1;
//    }
//
//    public void setObjectKey1(String objectKey1) {
//        this.objectKey1 = objectKey1;
//    }
//
//    public String getObjectKey2() {
//        return objectKey2;
//    }
//
//    public void setObjectKey2(String objectKey2) {
//        this.objectKey2 = objectKey2;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public boolean isActive() {
//        return isActive;
//    }
//
//    public void setActive(boolean isActive) {
//        this.isActive = isActive;
//    }
//
//    public String getHookConnector() {
//        return hookConnector;
//    }
//
//    public void setHookConnector(String HookConnector) {
//        this.hookConnector = HookConnector;
//    }
//
//    public String getHookRecipient() {
//        return hookRecipient;
//    }
//
//    public void setHookRecipient(String HookRecipient) {
//        this.hookRecipient = HookRecipient;
//    }
//
//    public String getHookChannel() {
//        return hookChannel;
//    }
//
//    public void setHookChannel(String HookChannel) {
//        this.hookChannel = HookChannel;
//    }
//
//    public String getUsrCreated() {
//        return usrCreated;
//    }
//
//    public void setUsrCreated(String usrCreated) {
//        this.usrCreated = usrCreated;
//    }
//
//    public String getUsrModif() {
//        return usrModif;
//    }
//
//    public void setUsrModif(String usrModif) {
//        this.usrModif = usrModif;
//    }
//
//    public Timestamp getDateCreated() {
//        return dateCreated;
//    }
//
//    public void setDateCreated(Timestamp dateCreated) {
//        this.dateCreated = dateCreated;
//    }
//
//    public Timestamp getDateModif() {
//        return dateModif;
//    }
//
//    public void setDateModif(Timestamp dateModif) {
//        this.dateModif = dateModif;
//    }

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

//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 29 * hash + Objects.hashCode(this.id);
//        hash = 29 * hash + Objects.hashCode(this.eventReference);
//        hash = 29 * hash + Objects.hashCode(this.objectKey1);
//        hash = 29 * hash + Objects.hashCode(this.objectKey2);
//        hash = 29 * hash + (this.isActive ? 1 : 0);
//        hash = 29 * hash + Objects.hashCode(this.hookConnector);
//        hash = 29 * hash + Objects.hashCode(this.hookRecipient);
//        hash = 29 * hash + Objects.hashCode(this.hookChannel);
//        hash = 29 * hash + Objects.hashCode(this.description);
//        return hash;
//    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final EventHook other = (EventHook) obj;
//        if (!Objects.equals(this.id, other.id)) {
//            return false;
//        }
//        if ((this.eventReference == null) ? (other.eventReference != null) : !this.eventReference.equals(other.eventReference)) {
//            return false;
//        }
//        if ((this.hookChannel == null) ? (other.hookChannel != null) : !this.hookChannel.equals(other.hookChannel)) {
//            return false;
//        }
//        if ((this.hookConnector == null) ? (other.hookConnector != null) : !this.hookConnector.equals(other.hookConnector)) {
//            return false;
//        }
//        if ((this.hookRecipient == null) ? (other.hookRecipient != null) : !this.hookRecipient.equals(other.hookRecipient)) {
//            return false;
//        }
//        if ((this.objectKey1 == null) ? (other.objectKey1 != null) : !this.objectKey1.equals(other.objectKey1)) {
//            return false;
//        }
//        if ((this.objectKey2 == null) ? (other.objectKey2 != null) : !this.objectKey2.equals(other.objectKey2)) {
//            return false;
//        }
//        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
//            return false;
//        }
//        if (this.isActive != other.isActive) {
//            return false;
//        }
//        return true;
//    }

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
