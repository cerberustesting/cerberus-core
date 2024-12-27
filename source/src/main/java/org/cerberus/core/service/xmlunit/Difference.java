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
package org.cerberus.core.service.xmlunit;

/**
 * Represents a difference that can be computed when comparing two data sources
 * 
 * <p>
 * A difference simply contains a {@link #getDiff()} String representation
 * </p>
 * 
 * @author abourdon
 */
public class Difference {

	/** The {@link String} representation of this {@link Difference} */
	private String diff;

	/**
	 * Creates a new {@link Difference} based on the given {@link String} representation
	 * 
	 * @param diff
	 *            the difference {@link String} representation
	 */
	public Difference(String diff) {
		this.diff = diff;
	}

	/**
	 * Gets the {@link String} representation of this {@link Difference}
	 * 
	 * @return the String representation of this {@link Difference}
	 */
	public String getDiff() {
		return diff;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diff == null) ? 0 : diff.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
                    return true;
                }
		if (obj == null) {
                    return false;
                }
		if (getClass() != obj.getClass()) {
                    return false;
                }
		Difference other = (Difference) obj;
		if (diff == null) {
			if (other.diff != null) {
                            return false;
                        }
		} else if (!diff.equals(other.diff)) {
                    return false;
                }
		return true;
	}

	@Override
	public String toString() {
		return "Difference [diff=" + diff + "]";
	}

}
