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

import org.cerberus.core.service.xmlunit.InputTranslator;
import org.cerberus.core.service.xmlunit.InputTranslatorUtil;

/**
 * Common implementation of a {@link InputTranslator}
 * 
 * @author abourdon
 *
 * @param <T>
 */
public abstract class AInputTranslator<T> implements InputTranslator<T> {

	/** Associated prefix to this {@link InputTranslator} */
	private String prefix;

	/**
	 * Creates a new {@link InputTranslator} identified by the given prefix
	 * 
	 * @param prefix
	 *            the prefix used to identified this {@link InputTranslator}
	 */
	public AInputTranslator(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public boolean canTranslate(String input) {
		return getPrefix() == null || getPrefix().equals(InputTranslatorUtil.getPrefix(input));
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

}
