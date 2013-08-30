package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IBatchInvariantDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.BatchInvariant;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryBatchInvariant;
import com.redcats.tst.factory.impl.FactoryBatchInvariant;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.ParameterParserUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Level;
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
        try {
            PreparedStatement preStat = this.databaseSpring.connect().prepareStatement(query);
            preStat.setString(1, batch);

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwEx = true;
                    }
                    result = this.loadBatchInvariantFromResultSet(resultSet);

                } catch (SQLException exception) {
                    MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(ApplicationDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            this.databaseSpring.disconnect();
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
