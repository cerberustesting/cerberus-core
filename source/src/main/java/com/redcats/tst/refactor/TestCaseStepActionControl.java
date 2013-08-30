package com.redcats.tst.refactor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class TestCaseStepActionControl implements DatabaseCRUD {

    private static final String COLUMNS = "`Test`, `TestCase`, `Step`, `Sequence`, `Control`, `Type`, `ControlValue`, `ControlProperty`, `Fatal`";
    private static final String TABLE = "testcasestepactioncontrol";
    private Integer control;
    private String controlProperty;
    private String controlValue;
    private final DbMysqlController db;
    private Integer sequence;
    private Integer step;
    private String test;
    private String testcase;
    private String type;
    private Boolean fatal;

    public Boolean isFatal() {
        return fatal;
    }

    public void setFatal(Boolean fatal) {
        this.fatal = fatal;
    }

    public TestCaseStepActionControl() {

        this.db = new DbMysqlController();
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
        try {
            ArrayList<String> al = new ArrayList<String>();
            al.add(this.test);
            al.add(this.testcase);
            al.add(this.step.toString());
            al.add(this.sequence.toString());
            al.add(this.control.toString());
            al.add(this.type);
            al.add(this.controlValue);
            al.add(this.controlProperty);
            al.add(lfatal);

            this.db.connect();
            this.db.update(sql.toString(), al);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.db.disconnect();
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

        try {
            ArrayList<String> al = new ArrayList<String>();
            al.add(this.type);
            al.add(this.controlValue);
            al.add(this.controlProperty);
            al.add(lfatal);
            al.add(this.test);
            al.add(this.testcase);
            al.add(this.step.toString());
            al.add(this.sequence.toString());
            al.add(this.control.toString());

            this.db.connect();
            this.db.update(sql.toString(), al);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.db.disconnect();
        }

    }
}
