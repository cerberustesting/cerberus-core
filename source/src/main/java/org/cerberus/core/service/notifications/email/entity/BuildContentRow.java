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
package org.cerberus.core.service.notifications.email.entity;

/**
 * Line of the build content table of the new build/revision email.
 *
 * @param build build (sprint) name
 * @param revision revision name
 * @param application application concerned
 * @param project project of the change
 * @param bugId bug fixed
 * @param bugUrl link to the bug (empty when the application has no bug tracker)
 * @param ticketId ticket fixed
 * @param owner person in charge
 * @param release release label
 * @param releaseUrl link to the release documentation (empty if none)
 * @param subject description of the change
 */
public record BuildContentRow(String build, String revision, String application, String project, String bugId, String bugUrl,
        String ticketId, String owner, String release, String releaseUrl, String subject) {
}
