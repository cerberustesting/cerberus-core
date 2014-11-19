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
package org.cerberus.util;

import javax.xml.XMLConstants;

import junit.framework.Assert;

import org.cerberus.util.XmlUtil.UniversalNamespaceCache;
import org.junit.Test;

/**
 * {@link UniversalNamespaceCache} test suite
 * 
 * @author abourdon
 */
public class XmlUtilUniversalNamespaceCacheTest {

	@Test
	public void testUniversalNamespaceCache() throws XmlUtilException {
		UniversalNamespaceCache cache = new UniversalNamespaceCache(XmlUtil.fromURL(getClass().getResource("namespaces.xml")));
		Assert.assertEquals("http://default", cache.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
		Assert.assertEquals("http://prefix", cache.getNamespaceURI("prefix"));
		Assert.assertEquals("http://other", cache.getNamespaceURI("other"));
	}

}
