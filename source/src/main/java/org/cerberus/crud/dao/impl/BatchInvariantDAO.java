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
package org.cerberus.crud.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.cerberus.crud.dao.IBatchInvariantDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.BatchInvariant;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryBatchInvariant;
import org.cerberus.crud.factory.impl.FactoryBatchInvariant;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BatchInvariantDAO implements IBatchInvariantDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryBatchInvariant factoryBatchInvariant;

    @Override
    public BatchInvariant findBatchInvariantByKey(String batch) throws CerberusException {
        boolean throwEx = false;
        BatchInvariant result = null;
        final String query = "SELECT * FROM batchinvariant a WHERE a.batch = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, batch);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwEx = true;
                    }
                    result = this.loadBatchInvariantFromResultSet(resultSet);

                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    private BatchInvariant loadBatchInvariantFromResultSet(ResultSet rs) throws SQLException {
        String batch = ParameterParserUtil.parseStringParam(rs.getString("Batch"), "");
        String incIni = ParameterParserUtil.parseStringParam(rs.getString("IncIni"), "");
        String unit = ParameterParserUtil.parseStringParam(rs.getString("Unit"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("Description"), "");

        //TODO remove when working in test with mockito and autowired
        factoryBatchInvariant = new FactoryBatchInvariant();
        return factoryBatchInvariant.create(batch, incIni, unit, description);
    }
}
