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
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for SqlUtil utility class
 *
 * @author memiks
 * @version 1.0, 11/02/2014
 */
public class SqlUtilTest {

    @Test
    public void testGenerateEmptyStringWhenProvideNullFieldToCreateWhereInClause() {
        String result;
        boolean expected = true;
        
        result = SqlUtil.createWhereInClause(null, new ArrayList<String>(), false);

        Assert.assertEquals(expected, result.isEmpty());
    }

    @Test
    public void testGenerateEmptyStringWhenProvideEmptyFieldToCreateWhereInClause() {
        String result;
        boolean expected = true;

        result = SqlUtil.createWhereInClause("", new ArrayList<String>(), false);

        Assert.assertEquals(expected, result.isEmpty());
    }

    @Test
    public void testGenerateEmptyStringWhenProvideNullValueToCreateWhereInClause() {
        String result;
        boolean expected = true;

        result = SqlUtil.createWhereInClause("test", null, false);

        Assert.assertEquals(expected, result.isEmpty());
    }

    @Test
    public void testGenerateEmptyStringWhenProvideEmptyValueToCreateWhereInClause() {
        String result;
        boolean expected = true;

        result = SqlUtil.createWhereInClause("test", new ArrayList<String>(), false);

        Assert.assertEquals(expected, result.isEmpty());
    }

    @Test
    public void testGenerateStringWithOneValueAndNoStringTypeToCreateWhereInClause() {
        String result;
        String expected = "Field IN (1)";

        List<String> values = new ArrayList<String>();
        values.add("1");

        result = SqlUtil.createWhereInClause("Field", values, false);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGenerateStringWithOneValueAndStringTypeToCreateWhereInClause() {
        String result;
        String expected = "Field IN ('1')";

        List<String> values = new ArrayList<String>();
        values.add("1");

        result = SqlUtil.createWhereInClause("Field", values, true);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGenerateStringWithNoStringTypeToCreateWhereInClause() {
        String result;
        String expected = "Field IN (1, 2, 3)";

        List<String> values = new ArrayList<String>();
        values.add("1");
        values.add("2");
        values.add("3");

        result = SqlUtil.createWhereInClause("Field", values, false);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGenerateStringWithStringTypeToCreateWhereInClause() {
        String result;
        String expected = "Field IN ('1', '2', '3')";

        List<String> values = new ArrayList<String>();
        values.add("1");
        values.add("2");
        values.add("3");

        result = SqlUtil.createWhereInClause("Field", values, true);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGenerateStringWithOneNullValueAndNoStringTypeToCreateWhereInClause() {
        String result;
        String expected = "Field IN (1, 3)";

        List<String> values = new ArrayList<String>();
        values.add("1");
        values.add(null);
        values.add("3");

        result = SqlUtil.createWhereInClause("Field", values, false);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testGenerateStringWithOneNullValueAndStringTypeToCreateWhereInClause() {
        String result;
        String expected = "Field IN ('1', '3')";

        List<String> values = new ArrayList<String>();
        values.add("1");
        values.add(null);
        values.add("3");

        result = SqlUtil.createWhereInClause("Field", values, true);

        Assert.assertEquals(expected, result);
    }

}
