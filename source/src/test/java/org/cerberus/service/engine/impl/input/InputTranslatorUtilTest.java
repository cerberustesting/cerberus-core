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
package org.cerberus.service.engine.impl.input;

import org.cerberus.service.engine.impl.input.InputTranslatorUtil;
import junit.framework.Assert;

import org.junit.Test;

/**
 * {@link InputTranslatorUtil} unit tests
 * 
 * @author abourdon
 */
public class InputTranslatorUtilTest {

	private static String createInput(String prefix, String value) {
		if (value == null) {
			return prefix;
		}
		StringBuilder builder = new StringBuilder(prefix);
		builder.append(InputTranslatorUtil.DELIMITER);
		builder.append(value);
		return builder.toString();
	}

	public InputTranslatorUtilTest() {

	}

	@Test
	public void testGetPrefixWithPrefix() {
		String prefix = "prefix";
		String value = "value";
		String input = createInput(prefix, value);
		Assert.assertEquals(prefix, InputTranslatorUtil.getPrefix(input));
	}

	@Test
	public void testGetPrefixWithoutPrefix() {
		String input = "input";
		Assert.assertEquals(null, InputTranslatorUtil.getPrefix(input));
	}

	@Test
	public void testGetValueWithoutPrefix() {
		String input = "input";
		Assert.assertEquals(input, InputTranslatorUtil.getValue(input));
	}

	@Test
	public void testGetValueWithoutAdditionalDelimiter() {
		String prefix = "prefix";
		String value = "value";
		String input = createInput(prefix, value);
		Assert.assertEquals(value, InputTranslatorUtil.getValue(input));
	}

	@Test
	public void testGetValueWithAdditionalDelimiter() {
		String prefix = "prefix";
		String value = "value" + InputTranslatorUtil.DELIMITER + "value";
		String input = createInput(prefix, value);
		Assert.assertEquals(value, InputTranslatorUtil.getValue(input));
	}
}
