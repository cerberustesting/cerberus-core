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

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for {@link InputTranslator}.
 * 
 * @author abourdon
 *
 */
public final class InputTranslatorUtil {

	public static final String DELIMITER = "=";

	/**
	 * Gets prefix from the given data input.
	 * 
	 * @param input
	 *            the data input to get prefix.
	 * @return the prefix contained into the data input, or <code>null</code> if not existing.
	 */
	public static String getPrefix(String input) {
		if (!isPrefixed(input)) {
			return null;
		}
		return input.split(DELIMITER)[0];
	}

	/**
	 * Gets value from the given data input.
	 * 
	 * <p>
	 * Entire data input is returned if data input is not prefixed.
	 * </p>
	 * 
	 * @param input
	 *            the data input to get value.
	 * @return the value from the given data input.
	 */
	public static String getValue(String input) {
		if (!isPrefixed(input)) {
			return input;
		}
		String[] splitInput = input.split(DELIMITER);
		if (splitInput.length == 2) {
			return splitInput[1];
		}
		return StringUtils.join(splitInput, DELIMITER, 1, splitInput.length);
	}

	/**
	 * Checks if the data input is prefixed or not.
	 * 
	 * @param input
	 *            the data input to test.
	 * @return <code>true</code> if the given data input is prefixed, <code>false</code> otherwise.
	 */
	private static boolean isPrefixed(String input) {
		return input.contains(DELIMITER);
	}

	/**
	 * Utility class then private constructor.
	 */
	private InputTranslatorUtil() {

	}

}
