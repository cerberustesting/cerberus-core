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
package org.cerberus.crud.service;

import java.util.List;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IInvariantService {

    Invariant findInvariantByIdValue(String idName, String value) throws CerberusException;

    Invariant findInvariantByIdSort(String idName, Integer sort) throws CerberusException;

    AnswerList readByIdname(String idName);

    AnswerList findInvariantByIdGp1(String idName, String gp);

    AnswerList readInvariantCountryListEnvironmentLastChanges(String system, Integer nbDays);

    AnswerList readByPublicByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    AnswerList readByPrivateByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    AnswerList readByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    Integer getNumberOfPrivateInvariant(String searchTerm);

    Integer getNumberOfPublicInvariant(String searchTerm);

    boolean isInvariantExist(String idName, String value);

    void createInvariant(Invariant invariant) throws CerberusException;

    void deleteInvariant(Invariant invariant) throws CerberusException;

    void updateInvariant(Invariant invariant) throws CerberusException;

    String getPublicPrivateFilter(String filter);
}
