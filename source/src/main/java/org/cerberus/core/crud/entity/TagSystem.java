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

/**
 * @author vertigo
 */
public class TagSystem {

    private String tag;
    private String system;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getUsrCreated() {
        return UsrCreated;
    }

    public void setUsrCreated(String UsrCreated) {
        this.UsrCreated = UsrCreated;
    }

    public Timestamp getDateCreated() {
        return DateCreated;
    }

    public void setDateCreated(Timestamp DateCreated) {
        this.DateCreated = DateCreated;
    }

    public String getUsrModif() {
        return UsrModif;
    }

    public void setUsrModif(String UsrModif) {
        this.UsrModif = UsrModif;
    }

    public Timestamp getDateModif() {
        return DateModif;
    }

    public void setDateModif(Timestamp DateModif) {
        this.DateModif = DateModif;
    }

    public boolean hasSameKey(TagSystem obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final TagSystem other = obj;
        if ((this.tag == null) ? (other.tag != null) : !this.tag.equals(other.tag)) {
            return false;
        }
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 67 * hash + (this.tag != null ? this.tag.hashCode() : 0);
        hash = 67 * hash + (this.system != null ? this.system.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TagSystem other = (TagSystem) obj;
        if ((this.tag == null) ? (other.tag != null) : !this.tag.equals(other.tag)) {
            return false;
        }
        if ((this.system == null) ? (other.system != null) : !this.system.equals(other.system)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return system + tag;
    }
}
