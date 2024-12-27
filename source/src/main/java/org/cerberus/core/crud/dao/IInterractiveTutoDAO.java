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

import org.cerberus.core.crud.entity.InteractiveTuto;

import java.util.List;

public interface IInterractiveTutoDAO {

    /**
     *
     * @param id
     *          the step id
     * @param withStep
     *          if true, return all step associated to the tuto. If false, list is null
     * @return
     */
    public InteractiveTuto getInteractiveTutorial(int id, boolean withStep, String lang);

    /**
     * Return all step available
     * @param id
     *          The step id
     * @return
     *          if true, return all step associated to the tuto. If false, list is null
     */
    public List<InteractiveTuto> getListInteractiveTutorial(boolean withStep, String lang);
}
