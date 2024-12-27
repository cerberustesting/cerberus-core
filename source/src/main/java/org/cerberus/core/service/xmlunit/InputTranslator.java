/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.service.xmlunit;

/**
 * A {@link InputTranslator} is used to translate a data input based on its format.
 * 
 * <p>
 * A data input format respects the following pattern: <code>(prefix=)?value</code>.
 * </p>
 * 
 * <p>
 * Prefix is used to let value to be represented by using a given format, e.g, URL.
 * </p>
 * 
 * <p>
 * A {@link InputTranslator} can so translate a data input which contains a given prefix. Thus, it is defined by a given {@link #getPrefix()} and translate data input by using the
 * {@link #translate(String)} method
 * </p>
 * 
 * @author abourdon
 *
 * @param <T>
 *            the translate result type
 */
public interface InputTranslator<T> {

	/**
	 * If this {@link InputTranslator} can translate the given data input which follows a given format.
	 * 
	 * <p>
	 * Note that a <code>null</code> prefixed {@link InputTranslator} can always translate an input.
	 * </p>
	 * 
	 * @param input
	 *            the data input to test.
	 * @return <code>true</code> if this {@link InputTranslator} can translate the given data input, <code>false</code> otherwise.
	 */
	boolean canTranslate(String input);

	/**
	 * Gets the associated prefix to this {@link InputTranslator}
	 * 
	 * @return the associated prefix to this {@link InputTranslator}
	 */
	String getPrefix();

	/**
	 * Translates the given data input
	 * 
	 * @param input
	 *            the data input to translate
	 * @return the translation result
	 * @throws InputTranslatorException
	 *             if data input does not respect format used by this {@link InputTranslator}
	 */
	T translate(String input) throws InputTranslatorException;

}
