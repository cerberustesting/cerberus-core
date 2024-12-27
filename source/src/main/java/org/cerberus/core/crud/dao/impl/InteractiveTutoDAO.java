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
package org.cerberus.core.crud.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.dao.IInterractiveTutoDAO;
import org.cerberus.core.crud.entity.InteractiveTuto;
import org.cerberus.core.crud.entity.InteractiveTutoStep;
import org.cerberus.core.crud.entity.InteractiveTutoStepType;
import org.cerberus.core.crud.factory.impl.FactoryInteractiveTuto;
import org.cerberus.core.crud.factory.impl.FactoryInteractiveTutoStep;
import org.cerberus.core.database.DatabaseSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@Repository
public class InteractiveTutoDAO implements IInterractiveTutoDAO {

    private static final Logger LOG = LogManager.getLogger(InteractiveTutoDAO.class);

    @Autowired
    private DatabaseSpring databaseSpring;

    @Autowired
    private FactoryInteractiveTuto factoryIT;

    @Autowired
    private FactoryInteractiveTutoStep factoryITStep;

    @Override
    public InteractiveTuto getInteractiveTutorial(int id, boolean withStep, String lang) {

        final String query = "SELECT it.id, docTitle.docLabel as title, it.titleTranslationLabel,  doc.docLabel as description, it.translationLabel,  it.role, it.ord, it.level "
                + "FROM interactive_tuto it  "
                + "left outer JOIN documentation doc on doc.doctable='interactiveTuto' and doc.docfield=it.translationLabel and doc.lang=? "
                + "left outer JOIN documentation docTitle on docTitle.doctable='interactiveTuto' and docTitle.docfield=it.titleTranslationLabel and docTitle.lang=? "
                + "WHERE it.id = ?";

        InteractiveTuto tuto = null;
        try (
                Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            preStat.setString(1, lang);
            preStat.setString(2, lang);
            preStat.setInt(3, id);

            try (ResultSet resultSet = preStat.executeQuery()) {
                if (resultSet.first()) {
                    tuto = getInteractiveTutoFromResultset(resultSet, withStep, lang);
                }
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString(), exception);
        } finally {
            this.databaseSpring.closeConnection();
        }
        return tuto;
    }

    public List<InteractiveTutoStep> getListStep(int idInteractiveTuto, String lang) {
        final String query = "SELECT its.id, its.selector, doc.docLabel as description,  its.type, its.attr1 "
                + "FROM interactive_tuto_step its "
                + "left outer JOIN documentation doc on doc.doctable='interactiveTutoStep' and doc.docfield=concat(its.id_interactive_tuto,'.step.',its.step_order)  and doc.lang=? "
                + "WHERE its.id_interactive_tuto = ? "
                + "order by step_order";

        List<InteractiveTutoStep> tuto = new LinkedList<>();

        try (
                Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            preStat.setString(1, lang);
            preStat.setInt(2, idInteractiveTuto);
            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    int idStep = resultSet.getInt("id");
                    String selector = resultSet.getString("selector");
                    String description = resultSet.getString("description");
                    if (description == null || description.equals("null")) {
                        description = "no translation for this language";
                    }
                    String type = resultSet.getString("type");
                    String attr1 = resultSet.getString("attr1");

                    tuto.add(factoryITStep.create(idStep, selector, description, InteractiveTutoStepType.getEnum(type), attr1));
                }
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString(), exception);
        } finally {
            this.databaseSpring.closeConnection();
        }

        return tuto;
    }

    @Override
    public List<InteractiveTuto> getListInteractiveTutorial(boolean withStep, String lang) {
        final String query = "SELECT id, docTitle.docLabel as title, it.titleTranslationLabel, doc.docLabel as description, it.translationLabel, role, ord, level "
                + "FROM interactive_tuto it "
                + "left outer JOIN documentation doc on doc.doctable='interactiveTuto' and doc.docfield=it.translationLabel and doc.lang=? "
                + "left outer JOIN documentation docTitle on docTitle.doctable='interactiveTuto' and docTitle.docfield=it.titleTranslationLabel and docTitle.lang=? ";

        List<InteractiveTuto> res = new LinkedList<>();

        try (
                Connection connection = this.databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query);) {
            preStat.setString(1, lang);
            preStat.setString(2, lang);

            try (ResultSet resultSet = preStat.executeQuery()) {
                while (resultSet.next()) {
                    res.add(getInteractiveTutoFromResultset(resultSet, withStep, lang));
                }
            }
        } catch (SQLException exception) {
            LOG.warn("Unable to execute query : " + exception.toString(), exception);
        } finally {
            this.databaseSpring.closeConnection();
        }

        return res;
    }

    private InteractiveTuto getInteractiveTutoFromResultset(ResultSet rs, boolean withStep, String lang) throws SQLException {
        int idTuto = rs.getInt("id");
        String title = rs.getString("title");
        if (title == null || title.equals("null")) {
            title = "?" + rs.getString("titleTranslationLabel") + "?";
        }

        String description = rs.getString("description");
        if (description == null || description.equals("null")) {
            description = "?" + rs.getString("translationLabel") + "?";
        }

        String role = rs.getString("role");
        int order = rs.getInt("ord");
        int level = rs.getInt("level");
        InteractiveTuto tuto = factoryIT.create(idTuto, title, description, role, order, level);

        if (withStep) {
            tuto.setSteps(getListStep(idTuto, lang));
        }

        return tuto;
    }
}
