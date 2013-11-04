/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao.impl;

import com.redcats.tst.dao.IBuildRevisionInvariantDAO;
import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.entity.BuildRevisionInvariant;
import com.redcats.tst.entity.MessageGeneral;
import com.redcats.tst.entity.MessageGeneralEnum;
import com.redcats.tst.exception.CerberusException;
import com.redcats.tst.factory.IFactoryBuildRevisionInvariant;
import com.redcats.tst.factory.impl.FactoryBuildRevisionInvariant;
import com.redcats.tst.log.MyLogger;
import com.redcats.tst.util.ParameterParserUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class BuildRevisionInvariantDAO implements IBuildRevisionInvariantDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryBuildRevisionInvariant factoryBuildRevisionInvariant;
    
    @Override
    public BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, Integer seq) throws CerberusException {
        boolean throwEx = false;
        BuildRevisionInvariant result = null;
        final String query = "SELECT * FROM buildrevisioninvariant a WHERE a.system = ? and a.level = ? and a.seq = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                preStat.setInt(2, level);
                preStat.setInt(3, seq);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwEx = true;
                    }
                    result = this.loadBuildRevisionInvariantFromResultSet(resultSet);

                } catch (SQLException exception) {
                    MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public BuildRevisionInvariant findBuildRevisionInvariantByKey(String system, Integer level, String versionName) throws CerberusException {
        boolean throwEx = false;
        BuildRevisionInvariant result = null;
        final String query = "SELECT * FROM buildrevisioninvariant a WHERE a.system = ? and a.level = ? and a.versionName = ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                preStat.setInt(2, level);
                preStat.setString(3, versionName);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (!(resultSet.first())) {
                        throwEx = true;
                    }
                    result = this.loadBuildRevisionInvariantFromResultSet(resultSet);

                } catch (SQLException exception) {
                    MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystemLevel(String system, Integer level) throws CerberusException {
        List<BuildRevisionInvariant> list = null;
        final String query = "SELECT * FROM buildrevisioninvariant WHERE `system` = ? and `level` = ? ORDER BY `seq`";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                preStat.setInt(2, level);
                
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<BuildRevisionInvariant>();
                    while (resultSet.next()) {
                        BuildRevisionInvariant buildRevisionInvariant = this.loadBuildRevisionInvariantFromResultSet(resultSet);
                        list.add(buildRevisionInvariant);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public List<BuildRevisionInvariant> findAllBuildRevisionInvariantBySystem(String system) throws CerberusException {
        List<BuildRevisionInvariant> list = null;
        final String query = "SELECT * FROM buildrevisioninvariant WHERE `system` = ? ORDER BY level, seq";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, system);
                
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<BuildRevisionInvariant>();
                    while (resultSet.next()) {
                        BuildRevisionInvariant buildRevisionInvariant = this.loadBuildRevisionInvariantFromResultSet(resultSet);
                        list.add(buildRevisionInvariant);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public boolean insertBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        boolean bool = false;
        final String query = "INSERT INTO buildrevisioninvariant (system, level, seq, versionname) VALUES (?, ?, ?, ?)";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            try {
                preStat.setString(1, buildRevisionInvariant.getSystem());
                preStat.setInt(2, buildRevisionInvariant.getLevel());
                preStat.setInt(3, buildRevisionInvariant.getSeq());
                preStat.setString(4, buildRevisionInvariant.getVersionName());

                preStat.executeUpdate();
                ResultSet resultSet = preStat.getGeneratedKeys();
                try {
                    if (resultSet.first()) {
                        bool = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public boolean deleteBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        boolean bool = false;
        final String query = "DELETE FROM buildrevisioninvariant WHERE system = ? and level= ? and seq = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, buildRevisionInvariant.getSystem());
                preStat.setInt(1, buildRevisionInvariant.getLevel());
                preStat.setInt(1, buildRevisionInvariant.getSeq());

                bool = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public boolean updateBuildRevisionInvariant(BuildRevisionInvariant buildRevisionInvariant) {
        boolean bool = false;
        final String query = "UPDATE buildrevisioninvariant SET versionname = ?  WHERE system = ? and level = ? and seq = ?, ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, buildRevisionInvariant.getVersionName());
                preStat.setString(2, buildRevisionInvariant.getSystem());
                preStat.setInt(3, buildRevisionInvariant.getLevel());
                preStat.setInt(4, buildRevisionInvariant.getSeq());

                bool = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(BuildRevisionInvariantDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    private BuildRevisionInvariant loadBuildRevisionInvariantFromResultSet(ResultSet resultSet) throws SQLException {
        String system = ParameterParserUtil.parseStringParam(resultSet.getString("system"), "");
        Integer level = ParameterParserUtil.parseIntegerParam(resultSet.getString("level"), 0);
        Integer seq = ParameterParserUtil.parseIntegerParam(resultSet.getString("seq"), 0);
        String versionname = ParameterParserUtil.parseStringParam(resultSet.getString("versionname"), "");

        factoryBuildRevisionInvariant = new FactoryBuildRevisionInvariant();
        return factoryBuildRevisionInvariant.create(system, level, seq, versionname);
    }
    
}
