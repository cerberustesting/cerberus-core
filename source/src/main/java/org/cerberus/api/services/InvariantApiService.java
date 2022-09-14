/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.api.services;

import lombok.AllArgsConstructor;
import org.cerberus.api.dao.DAO;
import org.cerberus.api.exceptions.EntityNotFoundException;
import org.cerberus.crud.dao.IInvariantDAO;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.exception.CerberusException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mlombard
 */
@AllArgsConstructor
@Service
public class InvariantApiService {

    IInvariantDAO invariantDao;
    DAO<Invariant> invariantDAOJdbcTemplate;

    public Invariant readByKey(String idName, String value) throws CerberusException {
        return this.invariantDAOJdbcTemplate
                .findByKey(idName, value)
                .orElseThrow(() -> new EntityNotFoundException(Invariant.class, "idname", idName));
    }

    public List<Invariant> readyByIdName(String idName) {
        List<Invariant> invariants = this.invariantDAOJdbcTemplate.findByIdName(idName);
        if (invariants == null || invariants.isEmpty()) {
            throw new EntityNotFoundException(Invariant.class, "idName", idName);
        }
        return invariants;
    }

    public List<Invariant> findAll() {
        return this.invariantDAOJdbcTemplate.list();
    }

    public List<Invariant> create(Invariant invariant) {
        this.invariantDAOJdbcTemplate.create(invariant);
        return this.findAll();
    }
}
