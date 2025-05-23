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

import lombok.*;
import org.json.JSONObject;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPromptMessage {

    private int id;
    private String sessionID;
    private String role;
    private String message;
    private String usrCreated;
    private Timestamp dateCreated;
    private String usrModif;
    private Timestamp dateModif;

    public JSONObject toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("id", id);
        jo.put("sessionID", sessionID);
        jo.put("role", role);
        jo.put("message", message);
        jo.put("usrCreated", usrCreated);
        jo.put("dateCreated", dateCreated);
        jo.put("usrModif", usrModif);
        jo.put("dateModif", dateModif);
        return jo;
    }
}
