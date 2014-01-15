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
