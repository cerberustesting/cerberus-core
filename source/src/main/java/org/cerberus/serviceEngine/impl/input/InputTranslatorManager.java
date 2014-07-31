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

import java.util.ArrayList;
import java.util.List;

public class InputTranslatorManager<T> {

	private List<InputTranslator<T>> translators;

	public InputTranslatorManager() {
		translators = new ArrayList<InputTranslator<T>>();
	}

	public void addTranslator(InputTranslator<T> translator) {
		if (!translators.contains(translator)) {
			translators.add(translator);
		}
	}

	public void removeTranslator(InputTranslator<T> translator) {
		if (translators.contains(translator)) {
			translators.remove(translator);
		}
	}

	public InputTranslator<T> getTranslatorFromInput(String input) {
		for (InputTranslator<T> translator : translators) {
			if (translator.canTranslate(input)) {
				return translator;
			}
		}
		return null;
	}

	public T translate(String input) throws InputTranslatorException {
		InputTranslator<T> translator = getTranslatorFromInput(input);
		if (translator == null) {
			throw new InputTranslatorException("Unable to handle format for input " + input);
		}
		return translator.translate(input);
	}

}
