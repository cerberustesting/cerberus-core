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
package org.cerberus.core.service.csvfile;

import java.util.HashMap;
import java.util.List;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface ICsvFileService {

    /**
     *
     * @param urlToCSVFile
     * @param separator
     * @param ignoreFirstLine
     * @param columnsToGet
     * @param columnsToHide
     * @param ignoreNoMatchColumns if <code>true</code> then populate all
     * non-matched column with {@link String} empty value (""). Otherwise, all
     * non-matched columns will not be included in the answer
     * @param defaultNoMatchColumnValue the default value to set to any
     * non-matched column if necessary
     * @param execution
     * @return
     */
    AnswerList<HashMap<String, String>> parseCSVFile(String urlToCSVFile, String separator, boolean ignoreFirstLine, HashMap<String, String> columnsToGet, List<String> columnsToHide, boolean ignoreNoMatchColumns, String defaultNoMatchColumnValue, TestCaseExecution execution);
}
