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
package org.cerberus.service;

import java.util.List;

import org.cerberus.entity.Invariant;
import org.cerberus.exception.CerberusException;

/**
 *
 * @author bcivel
 */
public interface IInvariantService {

    public Invariant findInvariantByIdValue(String idName, String value) throws CerberusException;

    public List<Invariant> findListOfInvariantById(String idName) throws CerberusException;

    public List<Invariant> findInvariantByIdGp1(String idName, String gp) throws CerberusException;

    public List<Invariant> findInvariantPublicListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    public List<Invariant> findInvariantPrivateListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch);

    public Integer getNumberOfPrivateInvariant();
    
    public Integer getNumberOfPublicInvariant();
    
    public boolean isInvariantExist(String idName, String value);
    
    String getPublicPrivateFilter(String filter);
}
