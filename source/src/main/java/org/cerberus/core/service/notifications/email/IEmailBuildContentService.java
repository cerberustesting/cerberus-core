/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
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
package org.cerberus.core.service.notifications.email;

import java.util.List;
import org.cerberus.core.service.notifications.email.entity.BuildContentRow;

public interface IEmailBuildContentService {

    /**
     * Read the modifications delivered between lastRevision (excluded) and revision (included) for the given build.
     * Never fails : an error is logged and an empty list is returned.
     *
     * @param system system
     * @param build build (sprint) delivered
     * @param revision last revision delivered
     * @param lastRevision previous revision (empty = everything since the beginning)
     * @return the lines, ordered by build, revision and application
     */
    List<BuildContentRow> read(String system, String build, String revision, String lastRevision);
}
