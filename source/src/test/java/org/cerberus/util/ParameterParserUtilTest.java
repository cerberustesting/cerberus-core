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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class that test the ParameterParserUtil
 *
 * @author Tiago Bernardes
 * @version 1.0, 02/07/2013
 * @see org.cerberus.util.ParameterParserUtil
 * @since 2.0.0
 */
public class ParameterParserUtilTest {

    @Test
    public void testParseIntegerParam() {
        int res = ParameterParserUtil.parseIntegerParam("12345", 0);

        Assert.assertEquals(12345, res);
    }

//    @Test
//    public void testParseIntegerParamWhenNullProvided() {
//        int res = ParameterParserUtil.parseIntegerParam(null, 0);
//
//        Assert.assertEquals(0, res);
//    }

    @Test
    public void testParseIntegerParamWhenEmptyStringProvided() {
        int res = ParameterParserUtil.parseIntegerParam("", 0);

        Assert.assertEquals(0, res);
    }

    @Test
    public void testParseIntegerParamWhenNonNumericStringProvided() {
        int res = ParameterParserUtil.parseIntegerParam("qwerty", 0);
        Assert.assertEquals(0, res);
    }

    @Test
    public void testParseStringParamAndDecode() {
        Assert.assertEquals("foo bar", ParameterParserUtil.parseStringParamAndDecodeAndSanitize("foo%20bar", "default", "UTF-8"));
    }

    @Test
    public void testParseStringParamAndDecodeWhenNull() {
        Assert.assertEquals("default", ParameterParserUtil.parseStringParamAndDecodeAndSanitize(null, "default", "UTF-8"));
    }

    @Test
    public void testParseIntegerParamAndDecode() {
        Assert.assertEquals(1, ParameterParserUtil.parseIntegerParamAndDecode("1", -1, "UTF-8"));
    }

    @Test
    public void testParseIntegerParamAndDecodeWithNull() {
        Assert.assertEquals(-1, ParameterParserUtil.parseIntegerParamAndDecode(null, -1, "UTF-8"));
    }

    @Test
    public void testParseIntegerParamAndDecodeWithNonNumeric() {
        Assert.assertEquals(-1, ParameterParserUtil.parseIntegerParamAndDecode("foo", -1, "UTF-8"));
    }

    @Test
    public void testParseLongParamAndDecode() {
        Assert.assertEquals(1L, ParameterParserUtil.parseLongParamAndDecode("1", -1L, "UTF-8"));
    }

    @Test
    public void testParseLongParamAndDecodeWithNull() {
        Assert.assertEquals(-1L, ParameterParserUtil.parseLongParamAndDecode(null, -1L, "UTF-8"));
    }

    @Test
    public void testParseLongParamAndDecodeWithNonNumeric() {
        Assert.assertEquals(-1L, ParameterParserUtil.parseLongParamAndDecode("foo", -1L, "UTF-8"));
    }

    @Test
    public void testParseListParamAndDecode() {
        List<String> actual = ParameterParserUtil.parseListParamAndDecode(new String[]{"fo%20o", "bar"}, Arrays.asList("default"), "UTF-8");
        List<String> expected = Arrays.asList("fo o", "bar");
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testParseListParamAndDecodeWithNull() {
        List<String> expected = Arrays.asList("default");
        List<String> actual = ParameterParserUtil.parseListParamAndDecode(null, expected, "UTF-8");
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testParseListMapParamAndDecode() {
        List<Map<String, String>> actual = ParameterParserUtil.parseListMapParamAndDecode(new String[]{"foo=bar&alice=bob", "bob=alice&foo=b%20ar"}, null, "UTF-8");

        Assert.assertEquals(2, actual.size());

        Map<String, String> actualMap = actual.get(0);
        Assert.assertEquals("bar", actualMap.get("foo"));
        Assert.assertEquals("bob", actualMap.get("alice"));

        actualMap = actual.get(1);
        Assert.assertEquals("alice", actualMap.get("bob"));
        Assert.assertEquals("b ar", actualMap.get("foo"));
    }

    @Test
    public void testParseListMapParamAndDecodeWithNull() {
        @SuppressWarnings("serial")
        List<Map<String, String>> expected = new ArrayList<Map<String, String>>() {
            {
                add(new HashMap<String, String>());
            }
        };
        List<Map<String, String>> actual = ParameterParserUtil.parseListMapParamAndDecode(null, expected, "UTF-8");
        Assert.assertEquals(expected, actual);
    }
}
