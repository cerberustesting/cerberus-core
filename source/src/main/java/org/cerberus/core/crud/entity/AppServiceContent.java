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
import lombok.Data;

/**
 * @author vertigo
 */
@Data
public class AppServiceContent {

    private String service;
    private String key;
    private String value;
    private int sort;
    private boolean isActive;
    private boolean isInherited;
    private String description;
    private String UsrCreated;
    private Timestamp DateCreated;
    private String UsrModif;
    private Timestamp DateModif;

    public boolean hasSameKey(AppServiceContent obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final AppServiceContent other = obj;
        if ((this.service == null) ? (other.service != null) : !this.service.equals(other.service)) {
            return false;
        }
        if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppServiceContent that = (AppServiceContent) o;
        return sort == that.sort && isActive == that.isActive && service.equals(that.service) && key.equals(that.key) && value.equals(that.value) && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, key, value, sort, isActive, description);
    }

    @Override
    public String toString() {
        return service + " - " + key + " - " + value;
    }
}
