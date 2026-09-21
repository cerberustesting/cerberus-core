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
package org.cerberus.core.crud.dao;

import org.cerberus.core.crud.entity.TestCaseContentMatch;

import java.util.List;

/**
 * Searches the text a testcase is made of — action and control operands, conditions, descriptions
 * and property values — for a literal string.
 *
 * <p>Answers the question nothing else can: where else does this appear. A selector that turns out
 * to be wrong, a URL that changed, a property that is going away — each of them is usually copied
 * across several testcases, and finding the copies by listing testcases and walking their steps is
 * both slow and easy to abandon halfway.</p>
 */
public interface ITestCaseContentSearchDAO {

    /**
     * Finds every place a string appears inside the testcases of a system.
     *
     * @param search     the literal text to look for; matched case-insensitively, anywhere in the
     *                   field.
     * @param systems    the systems to search; an empty list searches every system the caller can
     *                   already reach through the tool layer.
     * @param testFolder optional test folder to narrow to; empty searches all of them.
     * @param testcase   optional testcase to narrow to; empty searches all of them.
     * @param scopes     which kinds of text to search, from {@link TestCaseContentMatch.Scope}.
     * @param maxResults hard cap on the number of matches returned.
     * @return the matches, ordered by testcase then by position inside it.
     */
    List<TestCaseContentMatch> search(String search, List<String> systems, String testFolder, String testcase,
                                      List<TestCaseContentMatch.Scope> scopes, int maxResults);
}
