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
package org.cerberus.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ParamRequestMaker} test suite.
 * 
 * @author abourdon
 */
public class ParamRequestMakerTest {

	private ParamRequestMaker maker;

	@Before
	public void setUp() {
		maker = new ParamRequestMaker();
	}

	@Test
	public void testAddParam() {
		int initialSize = maker.size();
		maker.addParam("foo", "bar");
		Assert.assertEquals("bar", maker.getParam("foo"));
		Assert.assertEquals(initialSize + 1, maker.size());
	}

	@Test
	public void testAddParamWithNullValue() {
		int initialSize = maker.size();
		maker.addParam("foo", null);
		Assert.assertEquals(null, maker.getParam("foo"));
		Assert.assertEquals(initialSize, maker.size());
	}

	@Test
	public void testAddParamWithEmptyValue() {
		int initialSize = maker.size();
		maker.addParam("foo", "");
		Assert.assertEquals(null, maker.getParam("foo"));
		Assert.assertEquals(initialSize, maker.size());
	}

	@Test
	public void testRemoveParam() {
		maker.addParam("foo", "bar");
		int initialSize = maker.size();
		maker.removeParam("foo");
		Assert.assertEquals(null, maker.getParam("foo"));
		Assert.assertEquals(initialSize - 1, maker.size());
	}

	@Test
	public void testMkString() throws UnsupportedEncodingException {
		maker.addParam("foo", "bar");
		maker.addParam("alice", "bo b");
		String charset = "UTF-8";
		String expectedEncodingBar = URLEncoder.encode("bar", charset);
		String expectedEncodingBob = URLEncoder.encode("bo b", charset);

		String actual = maker.mkString(charset);
		Assert.assertTrue(("foo=" + expectedEncodingBar + "&alice=" + expectedEncodingBob).equals(actual)
				|| ("alice=" + expectedEncodingBob + "&foo=" + expectedEncodingBar).equals(actual));
	}

}
