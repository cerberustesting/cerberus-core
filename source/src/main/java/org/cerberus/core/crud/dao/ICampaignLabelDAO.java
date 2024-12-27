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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.entity.CampaignLabel;
import org.cerberus.core.util.answer.Answer;
import org.cerberus.core.util.answer.AnswerItem;
import org.cerberus.core.util.answer.AnswerList;

/**
 * Interface that defines the public methods to manage Application data on table
 * Insert, Delete, Update, Find
 *
 * @author tbernardes
 * @version 1.0, 15/10/13
 * @since 0.9.0
 */
public interface ICampaignLabelDAO {

    /**
     *
     * @param campaignLabelID
     * @return
     */
    AnswerItem<CampaignLabel> readByKeyTech(Integer campaignLabelID);

    /**
     *
     * @param campaign
     * @param LabelId
     * @return
     */
    AnswerItem<CampaignLabel> readByKey(String campaign, Integer LabelId);

    /**
     *
     * @param campaign
     * @param startPosition
     * @param length
     * @param columnName
     * @param sort
     * @param searchParameter
     * @param individualSearch
     * @return
     */
    AnswerList<CampaignLabel> readByVariousByCriteria(String campaign, int startPosition, int length, String columnName, String sort, String searchParameter, Map<String, List<String>> individualSearch);

    /**
     *
     * @param object
     * @return
     */
    Answer create(CampaignLabel object);

    /**
     *
     * @param object
     * @return
     */
    Answer delete(CampaignLabel object);

    /**
     *
     * @param object
     * @return
     */
    Answer update(CampaignLabel object);

    /**
     * Uses data of ResultSet to create object {@link AppServiceContent}
     *
     * @param rs ResultSet relative to select from table Application
     * @return object {@link AppServiceContent}
     * @throws SQLException when trying to get value from
     * {@link java.sql.ResultSet#getString(String)}
     * @see FactoryApplication
     */
    CampaignLabel loadFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @param service
     * @param searchParameter
     * @param individualSearch
     * @param columnName
     * @return
     */
    public AnswerList<String> readDistinctValuesByCriteria(String service, String searchParameter, Map<String, List<String>> individualSearch, String columnName);

}
