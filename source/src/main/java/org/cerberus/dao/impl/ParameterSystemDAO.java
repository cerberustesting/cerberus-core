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
package org.cerberus.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.dao.IParameterSystemDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.entity.ParameterSystem;
import org.cerberus.exception.CerberusException;
import org.cerberus.factory.IFactoryParameterSystem;
import org.cerberus.log.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/03/2013
 * @since 2.0.0
 */
@Repository
public class ParameterSystemDAO implements IParameterSystemDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryParameterSystem factoryParameterSystem;

    @Override
    public List<ParameterSystem> findAllParameterSystem(String mySystem) throws CerberusException {
        boolean throwExep = true;
        List<ParameterSystem> result = null;
        ParameterSystem paramet;
        StringBuilder mySQL = new StringBuilder();
        mySQL.append("SELECT pC.param param, pC.`value` valC, pS.`value` valS, pC.description FROM parameter pC ");
        mySQL.append("LEFT OUTER JOIN ( SELECT * from parameter pS WHERE pS.system= ? ) as pS ON pS.param = pC.param ");
        mySQL.append(" WHERE pC.system= ''; ");
        final String query = mySQL.toString();

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, mySystem);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<ParameterSystem>();
                    while (resultSet.next()) {
                        String param = resultSet.getString("param");
                        String valueC = resultSet.getString("valC");
                        String valueS = resultSet.getString("valS");
                        String desc = resultSet.getString("description");
                        paramet = factoryParameterSystem.create(param, valueC, valueS, desc);
                        result.add(paramet);
                        throwExep = false;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(ParameterSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ParameterSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ParameterSystemDAO.class.getName(), Level.ERROR, "Unable to execute query : "+exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(ParameterSystemDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExep) {
            MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
            mes.setDescription(mes.getDescription() + " Parameter table empty.");
            throw new CerberusException(mes);
        }
        return result;
    }
}
