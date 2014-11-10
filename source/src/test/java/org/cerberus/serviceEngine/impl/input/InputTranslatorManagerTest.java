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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * {@link InputTranslatorManager} unit tests
 * 
 * @author abourdon
 */
public class InputTranslatorManagerTest {

	private InputTranslatorManager<String> translator;

	public InputTranslatorManagerTest() {
	}
	
	@Before
	public void setUp() {
		translator = new InputTranslatorManager<String>();
		translator.addTranslator(new AInputTranslator<String>("prefix") {

			@Override
			public String translate(String input) throws InputTranslatorException {
				return "main translator";
			}

		});
	}

	@Test
	public void testTranslateWithHandledPrefix() throws InputTranslatorException {
		translator.addTranslator(new AInputTranslator<String>(null) {

			@Override
			public String translate(String input) throws InputTranslatorException {
				return "second translator";
			}

		});
		
		Assert.assertEquals("main translator", translator.translate("prefix=value"));
		Assert.assertEquals("second translator", translator.translate("with_an_unknown_prefix=value"));
		Assert.assertEquals("second translator", translator.translate("wihtout prefix"));
	}

	@Test(expected = InputTranslatorException.class)
	public void testTranslateWithoutHandledPrefix() throws InputTranslatorException {
		translator.translate("with_an_unknown_prefix=value");
	}

}
