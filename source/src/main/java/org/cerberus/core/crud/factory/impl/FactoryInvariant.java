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
package org.cerberus.core.crud.factory.impl;

import org.cerberus.core.crud.entity.Invariant;
import org.cerberus.core.crud.factory.IFactoryInvariant;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryInvariant implements IFactoryInvariant {

    @Override
    public Invariant create(String idName, String value, Integer sort, String description, String veryShortDesc, String gp1, String gp2, String gp3,
            String gp4, String gp5, String gp6, String gp7, String gp8, String gp9) {
        Invariant invariant = new Invariant();
        invariant.setIdName(idName);
        invariant.setSort(sort);
        invariant.setValue(value);
        invariant.setDescription(description);
        invariant.setVeryShortDesc(veryShortDesc);
        invariant.setGp1(gp1);
        invariant.setGp2(gp2);
        invariant.setGp3(gp3);
        invariant.setGp4(gp4);
        invariant.setGp5(gp5);
        invariant.setGp6(gp6);
        invariant.setGp7(gp7);
        invariant.setGp8(gp8);
        invariant.setGp9(gp9);
        return invariant;
    }

}
