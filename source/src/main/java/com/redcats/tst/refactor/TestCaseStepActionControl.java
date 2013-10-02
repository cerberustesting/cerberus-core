package com.redcats.tst.refactor;

import com.redcats.tst.database.DatabaseSpring;
import com.redcats.tst.log.MyLogger;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestCaseStepActionControl implements DatabaseCRUD {

    private static final String COLUMNS = "`Test`, `TestCase`, `Step`, `Sequence`, `Control`, `Type`, `ControlValue`, `ControlProperty`, `Fatal`";
    private static final String TABLE = "testcasestepactioncontrol";
    private Integer control;
    private String controlProperty;
    private String controlValue;
    private Integer sequence;
    private Integer step;
    private String test;
    private String testcase;
    private String type;
    private Boolean fatal;

    @Autowired
    private DatabaseSpring databaseSpring;

    public Boolean isFatal() {
        return fatal;
    }

    public void setFatal(Boolean fatal) {
        this.fatal = fatal;
    }

    public TestCaseStepActionControl() {

        this.test = new String();
        this.testcase = new String();
        this.step = 0;
        this.sequence = 0;
        this.control = 0;
        this.type = new String();
        this.controlValue = new String();
        this.controlProperty = new String();
        this.fatal = true;
    }

    public Integer getControl() {

        return this.control;
    }

    public String getControlProperty() {

        return this.controlProperty;
    }

    public String getControlValue() {

        return this.controlValue;
    }

    public Integer getSequence() {

        return this.sequence;
    }

    public Integer getStep() {

        return this.step;
    }

    public String getTest() {

        return this.test;
    }

    public String getTestcase() {

        return this.testcase;
    }

    public String getType() {

        return this.type;
    }

    @Override
    public void importResultSet(ResultSet rs) {

        try {
            this.setTest(rs.getString("Test"));
            this.setTestcase(rs.getString("Testcase"));
            this.setStep(rs.getInt("Step"));
            this.setSequence(rs.getInt("Sequence"));
            this.setControl(rs.getInt("Control"));
            this.setType(rs.getString("Type"));
            this.setControlValue(rs.getString("ControlValue"));
            this.setControlProperty(rs.getString("ControlProperty"));
            this.setFatal(rs.getString("Fatal").compareTo("Y") == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert() {

        String lfatal;
        if (this.fatal) {
            lfatal = "Y";
        } else {
            lfatal = "N";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + TestCaseStepActionControl.TABLE + " ( "
                + TestCaseStepActionControl.COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.test);
                preStat.setString(2, this.testcase);
                preStat.setString(3, this.step.toString());
                preStat.setString(4, this.sequence.toString());
                preStat.setString(5, this.control.toString());
                preStat.setString(6, this.type);
                preStat.setString(7, this.controlValue);
                preStat.setString(8, this.controlProperty);
                preStat.setString(9, lfatal);

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.WARN, e.toString());
            }
        }

    }

    public void setControl(Integer control) {

        this.control = control;
    }

    public void setControlProperty(String controlProperty) {

        this.controlProperty = controlProperty;
    }

    public void setControlValue(String controlValue) {

        this.controlValue = controlValue;
    }

    public void setSequence(Integer sequence) {

        this.sequence = sequence;
    }

    public void setStep(Integer step) {

        this.step = step;
    }

    public void setTest(String test) {

        this.test = test;
    }

    public void setTestcase(String testcase) {

        this.testcase = testcase;
    }

    public void setType(String type) {

        this.type = type;
    }

    @Override
    public void update() {
        String lfatal;
        if (this.fatal) {
            lfatal = "Y";
        } else {
            lfatal = "N";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE " + TestCaseStepActionControl.TABLE + " SET ");

        sql.append(" Type = ? ,");
        sql.append(" ControlValue = ?,");
        sql.append(" ControlProperty = ? ,");
        sql.append(" Fatal = ? ");

        sql.append(" WHERE Test = ? AND TestCase = ? AND Step = ? AND Sequence = ? AND Control = ? ");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, this.type);
                preStat.setString(2, this.controlValue);
                preStat.setString(3, this.controlProperty);
                preStat.setString(4, lfatal);
                preStat.setString(5, this.test);
                preStat.setString(6, this.testcase);
                preStat.setString(7, this.step.toString());
                preStat.setString(8, this.sequence.toString());
                preStat.setString(9, this.control.toString());

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseStepActionControl.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseStepActionControl.class.getName(), Level.WARN, e.toString());
            }
        }
    }
}
