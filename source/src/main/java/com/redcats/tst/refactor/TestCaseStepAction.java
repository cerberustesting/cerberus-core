package com.redcats.tst.refactor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class TestCaseStepAction implements DatabaseCRUD {

    private static final String COLUMNS = "`Test`, `TestCase`, `Step`, `Sequence`, `Action`, `Object`, `Property`";
    private static final String TABLE = "testcasestepaction";
    private String action;
    private final DbMysqlController db;
    private String object;
    private String property;
    private Integer sequence;
    private Integer step;
    private String test;
    private String testcase;

    public TestCaseStepAction() {

        this.db = new DbMysqlController();
        this.property = new String();
        this.test = new String();
        this.testcase = new String();
        this.step = 0;
        this.sequence = 0;
        this.action = new String();
        this.object = new String();
        this.property = new String();

    }

    public String getAction() {

        return this.action;
    }

    public String getObject() {

        return this.object;
    }

    public String getProperty() {

        return this.property;
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

    @ Override
    public void importResultSet(ResultSet rs) {

        try {
            this.setTest(rs.getString("Test"));
            this.setTestcase(rs.getString("Testcase"));
            this.setStep(rs.getInt("Step"));
            this.setSequence(rs.getInt("Sequence"));
            this.setAction(rs.getString("Action"));
            this.setObject(rs.getString("Object"));
            this.setProperty(rs.getString("Property"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ Override
    public void insert() {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + TestCaseStepAction.TABLE + " ( " + TestCaseStepAction.COLUMNS + ") ");
        try {
            sql.append(" VALUES ( ?,?,?,?,?,?,?) ");

            ArrayList<String> al = new ArrayList<String>();
            al.add(this.test);
            al.add(this.testcase);
            al.add(this.step.toString());
            al.add(this.sequence.toString());
            al.add(this.action);
            al.add(this.object);
            al.add(this.property);

            this.db.connect();
            this.db.update(sql.toString(), al);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.db.disconnect();
        }

    }

    public void setAction(String action) {

        this.action = action;
    }

    public void setObject(String object) {

        this.object = object;
    }

    public void setProperty(String property) {

        this.property = property;
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

    @ Override
    public void update() {

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE " + TestCaseStepAction.TABLE + " SET ");

        sql.append(" Action = ? ,");
        sql.append(" Object = ?,");
        sql.append(" Property = ? ");

        sql.append(" WHERE Test = ? AND TestCase = ? AND Step = ? AND Sequence = ? ");

        try {
            ArrayList<String> al = new ArrayList<String>();
            al.add(this.action);
            al.add(this.object);
            al.add(this.property);
            al.add(this.test);
            al.add(this.testcase);
            al.add(this.step.toString());
            al.add(this.sequence.toString());

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
