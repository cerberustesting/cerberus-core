package com.redcats.tst.refactor;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class TestCaseCountryProperties implements DatabaseCRUD {

    private static final String COLUMNS = "`Test`, `TestCase`, `Country`, `Property`, `Type`, `Database`, `Value`, `Length`, `RowLimit`, `Nature`";
    private static final String TABLE = "testcasecountryproperties";
    private String country;
    private Integer length;
    private String nature;
    private String property;
    private Integer rowlimit;
    private String test;
    private String testcase;
    private String type;
    private String database;
    private String value;
    @Autowired
    private DatabaseSpring databaseSpring;

    public TestCaseCountryProperties() {

        this.country = new String();
        this.length = 0;
        this.nature = new String();
        this.property = new String();
        this.rowlimit = 0;
        this.test = new String();
        this.testcase = new String();
        this.type = new String();
        this.value = new String();
        this.database = new String();

    }

    public String getCountry() {

        return this.country;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public Integer getLength() {

        return this.length;
    }

    public void setLength(Integer length) {

        this.length = length;
    }

    public String getNature() {

        return this.nature;
    }

    public void setNature(String nature) {

        this.nature = nature;
    }

    public String getProperty() {

        return this.property;
    }

    public void setProperty(String property) {

        this.property = property;
    }

    public Integer getRowlimit() {

        return this.rowlimit;
    }

    public void setRowlimit(Integer rowlimit) {

        this.rowlimit = rowlimit;
    }

    public String getTest() {

        return this.test;
    }

    public void setTest(String test) {

        this.test = test;
    }

    public String getTestcase() {

        return this.testcase;
    }

    public void setTestcase(String testcase) {

        this.testcase = testcase;
    }

    public String getType() {

        return this.type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getDatabase() {

        return this.database;
    }

    public void setDatabase(String database) {

        this.database = database;
    }

    public String getValue() {

        return this.value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    @Override
    public void importResultSet(ResultSet rs) {

        try {
            this.setTest(rs.getString("Test"));
            this.setTestcase(rs.getString("Testcase"));
            this.setCountry(rs.getString("Country"));
            this.setProperty(rs.getString("Property"));
            this.setValue(rs.getString("Value"));
            this.setType(rs.getString("Type"));
            this.setRowlimit(rs.getInt("RowLimit"));
            this.setLength(rs.getInt("Length"));
            this.setNature(rs.getString("Nature"));
            this.setDatabase(rs.getString("Database"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert() {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + TestCaseCountryProperties.TABLE + " ( " + TestCaseCountryProperties.COLUMNS + ") ");
        sql.append(" VALUES ( ?,?,?,?,?,?,?,?,?,?) ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.test);
                preStat.setString(2, this.testcase);
                preStat.setString(3, this.country);
                preStat.setString(4, this.property);
                preStat.setString(5, this.type);
                preStat.setString(6, this.database);
                preStat.setString(7, this.value);
                preStat.setString(8, this.length.toString());
                preStat.setString(9, this.rowlimit.toString());
                preStat.setString(10, this.nature);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public void update() {

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE " + TestCaseCountryProperties.TABLE + " SET ");

        sql.append(" Type = ? ,");
        sql.append(" Database = ? ,");
        sql.append(" Value = ?,");
        sql.append(" Length = ?, ");
        sql.append(" RowLimit = ?, ");
        sql.append(" Nature = ? ");

        sql.append(" WHERE Test = ? AND TestCase = ? AND Country = ? AND Property = ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.type);
                preStat.setString(2, this.database);
                preStat.setString(3, this.value);
                preStat.setString(4, this.length.toString());
                preStat.setString(5, this.rowlimit.toString());
                preStat.setString(6, this.nature);
                preStat.setString(7, this.test);
                preStat.setString(8, this.testcase);
                preStat.setString(9, this.country);
                preStat.setString(10, this.property);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryProperties.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryProperties.class.getName(), Level.WARN, e.toString());
            }
        }
    }
}
