/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
package org.cerberus.dao;

import java.util.List;

import org.cerberus.entity.Project;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerList;

/**
 *
 * @author bcivel
 */
public interface IProjectDAO {

    Project findProjectByKey(String project);

    List<Project> findAllProject();

    public void createProject(Project project) throws CerberusException;

    public void deleteProject(Project project) throws CerberusException;

    public void updateProject(Project project) throws CerberusException;

    public AnswerList findProjectListByCriteria(int startPosition, int length, String columnName, String sort, String searchParameter, String string);
}
