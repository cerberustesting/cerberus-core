/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.serviceEngine.impl.input;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for {@link InputTranslator}
 * 
 * @author abourdon
 *
 */
public final class InputTranslatorUtil {

	public static final String DELIMITER = "=";

	public static String getPrefix(String input) {
		if (!isPrefixed(input)) {
			return null;
		}
		return input.split(DELIMITER)[0];
	}

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

	private static boolean isPrefixed(String input) {
		return input.contains(DELIMITER);
	}

	/**
	 * Utility class then private constructor
	 */
	private InputTranslatorUtil() {

	}

}
