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

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a list of {@link InputTranslator} and delegates its {@link #translate(String)} method to managed {@link InputTranslator}.
 * 
 * @author abourdon
 *
 * @param <T>
 *            the translate result type used by the list of {@link InputTranslator}
 */
public class InputTranslatorManager<T> {

	/** The {@link InputTranslator} list to manage */
	private List<InputTranslator<T>> translators;

	/**
	 * Creates a new {@link InputTranslatorManager}
	 */
	public InputTranslatorManager() {
		translators = new ArrayList<>();
	}

	/**
	 * Adds the new {@link InputTranslator} to be managed
	 * 
	 * @param translator
	 *            the new {@link InputTranslator} to be managed
	 */
	public void addTranslator(InputTranslator<T> translator) {
		if (!translators.contains(translator)) {
			translators.add(translator);
		}
	}

	/**
	 * Removes the given {@link InputTranslator} from this {@link InputTranslatorManager}
	 * 
	 * @param translator
	 *            the {@link InputTranslator} to remove from this {@link InputTranslatorManager}
	 */
	public void removeTranslator(InputTranslator<T> translator) {
		if (translators.contains(translator)) {
			translators.remove(translator);
		}
	}

	/**
	 * Gets the {@link InputTranslator} which can manage the given data input
	 * 
	 * @param input
	 *            the data input to use to find the associated {@link InputTranslator}
	 * @return the associated {@link InputTranslator} to the given data input, or <code>null</code> if no {@link InputTranslator} is found
	 */
	public InputTranslator<T> getTranslatorFromInput(String input) {
		for (InputTranslator<T> translator : translators) {
			if (translator.canTranslate(input)) {
				return translator;
			}
		}
		return null;
	}

	/**
	 * Translates the given data input by using managed {@link InputTranslator}
	 * 
	 * @param input
	 *            the data input to translate
	 * @return translation result
	 * @throws InputTranslatorException
	 *             if no {@link InputTranslator} can handle the given data input
	 * 
	 * @see InputTranslator#translate(String)
	 */
	public T translate(String input) throws InputTranslatorException {
		InputTranslator<T> translator = getTranslatorFromInput(input);
		if (translator == null) {
			throw new InputTranslatorException("Unable to handle format for input " + input);
		}
		return translator.translate(input);
	}

}
