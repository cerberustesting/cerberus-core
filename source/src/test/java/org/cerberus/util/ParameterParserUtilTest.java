package org.cerberus.util;

import org.cerberus.util.ParameterParserUtil;
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
        int res = ParameterParserUtil.parseIntegerParam("12345",0);

        Assert.assertEquals(12345, res);
    }

    @Test
    public void testParseIntegerParamWhenNullProvided() {
        int res = ParameterParserUtil.parseIntegerParam(null,0);

        Assert.assertEquals(0, res);
    }

    @Test
    public void testParseIntegerParamWhenEmptyStringProvided() {
        int res = ParameterParserUtil.parseIntegerParam("",0);

        Assert.assertEquals(0, res);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntegerParamWhenNonNumericStringProvided() throws NumberFormatException {
        ParameterParserUtil.parseIntegerParam("qwerty",0);
    }

}
