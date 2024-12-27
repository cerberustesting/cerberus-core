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
package org.cerberus.core.util.validity;

import java.util.ArrayList;
import java.util.List;

public class Validity {

    public static Validity valid() {
        return new Validity();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Validity validity;

        private Builder() {
            this.validity = new Validity();
        }

        public Builder valid(final boolean valid) {
            validity.setValid(valid);
            return this;
        }

        public Builder reason(final String reason) {
            validity.addReason(reason);
            return this;
        }

        public Builder merge(final Validity other) {
            if (!other.isValid()) {
                validity.setValid(false);
                validity.getReasons().addAll(other.getReasons());
            }
            return this;
        }

        public Validity build() {
            if (validity.isValid() && !validity.getReasons().isEmpty()) {
                validity.setValid(false);
            }
            return validity;
        }
    }

    private boolean valid = true;
    private List<String> reasons = new ArrayList<>();

    private Validity() {
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getReasons() {
        return reasons;
    }

    private void setValid(final boolean valid) {
        this.valid = valid;
    }

    private void addReason(final String reason) {
        reasons.add(reason);
    }

}
