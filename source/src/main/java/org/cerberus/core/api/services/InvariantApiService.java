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
package org.cerberus.core.api.services;

import lombok.AllArgsConstructor;
import org.cerberus.core.api.exceptions.EntityNotFoundException;
import org.cerberus.core.crud.dao.IInvariantDAO;
import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.exception.CerberusException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Service
public class InvariantApiService {

    IInvariantDAO invariantDao;

    public Invariant readByKey(String idName, String value) throws CerberusException {
        Invariant invariant = this.invariantDao.readByKey(idName, value);
        if (invariant == null) {
            throw new EntityNotFoundException(Invariant.class, "idName", idName, "value", value);
        }
        return invariant;
    }

    public List<Invariant> readyByIdName(String idName) throws CerberusException {
        List<Invariant> invariants = this.invariantDao.readByIdname(idName);
        if (invariants == null || invariants.isEmpty()) {
            throw new EntityNotFoundException(Invariant.class, "idName", idName);
        }
        return invariants;
    }
}
