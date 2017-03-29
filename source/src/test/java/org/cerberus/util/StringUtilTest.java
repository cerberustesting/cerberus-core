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


import junit.framework.Assert;

import org.junit.Test;

/**
 * Test class for StringUtil utility class
 *
 * @author Antoine Craske
 * @version 1.0, 07/01/2013
 * @since 2.0.0
 */
public class StringUtilTest {

    @Test
    public void testGenerateRandomStringReturnEmptyStringWhenLengthProvidedIsZero() {
        String charset = null;
        String result;
        int length = 0;
        boolean expected = true;
        
        result = StringUtil.getRandomString(length, charset);

        Assert.assertEquals(expected, result.isEmpty());
    }

    @Test
    public void testGenerateRandomStringReturnEmptyStringWhenNegativeLengthProvided() {
        String charset = null;
        String result;
        int length = -1;
        boolean expected = true;
        
        result = StringUtil.getRandomString(length, charset);

        Assert.assertEquals(expected, result.isEmpty());
    }
    
    @Test
    public void testGenerateRandomStringReturnStringWithLengthProvided() {
        String charset = "A";
        String result;
        int length = 1;
        int expected = 1;
        
        result = StringUtil.getRandomString(length, charset);

        Assert.assertEquals(expected, result.length());
    }
    
    @Test
    public void testGenerateRandomStringReturnTwoDifferentValues() {
        String charset = "A";
        String firstResult;
        String secondResult;
        int length = 1;
        
        firstResult = StringUtil.getRandomString(length, charset);
        secondResult = StringUtil.getRandomString(length, charset);
        
        Assert.assertNotSame(firstResult, secondResult);  
    }
    
}
