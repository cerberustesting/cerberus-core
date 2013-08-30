package com.redcats.tst.refactor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class TestCaseCountryProperties implements DatabaseCRUD {

    private static final String COLUMNS = "`Test`, `TestCase`, `Country`, `Property`, `Type`, `Database`, `Value`, `Length`, `RowLimit`, `Nature`";
    private static final String TABLE = "testcasecountryproperties";
    private String country;
    private final DbMysqlController db;
    private Integer length;
    private String nature;
    private String property;
    private Integer rowlimit;
    private String test;
    private String testcase;
    private String type;
    private String database;
    private String value;

    public TestCaseCountryProperties() {

        this.country = new String();
        this.db = new DbMysqlController();
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

    public Integer getLength() {

        return this.length;
    }

    public String getNature() {

        return this.nature;
    }

    public String getProperty() {

        return this.property;
    }

    public Integer getRowlimit() {

        return this.rowlimit;
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

    public String getDatabase() {

        return this.database;
    }

    public String getValue() {

        return this.value;
    }

    @ Override
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

    @ Override
    public void insert() {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + TestCaseCountryProperties.TABLE + " ( " + TestCaseCountryProperties.COLUMNS + ") ");
        try {
            sql.append(" VALUES ( ?,?,?,?,?,?,?,?,?,?) ");

            ArrayList<String> al = new ArrayList<String>();
            al.add(this.test);
            al.add(this.testcase);
            al.add(this.country);
            al.add(this.property);
            al.add(this.type);
            al.add(this.database);
            al.add(this.value);
            al.add(this.length.toString());
            al.add(this.rowlimit.toString());
            al.add(this.nature);

            this.db.connect();
            this.db.update(sql.toString(), al);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.db.disconnect();
        }

    }

    public void setCountry(String country) {

        this.country = country;
    }

    public void setLength(Integer length) {

        this.length = length;
    }

    public void setNature(String nature) {

        this.nature = nature;
    }

    public void setDatabase(String database) {

        this.database = database;
    }

    public void setProperty(String property) {

        this.property = property;
    }

    public void setRowlimit(Integer rowlimit) {

        this.rowlimit = rowlimit;
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

    public void setValue(String value) {

        this.value = value;
    }

    @ Override
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

        try {
            ArrayList<String> al = new ArrayList<String>();
            al.add(this.type);
            al.add(this.database);
            al.add(this.value);
            al.add(this.length.toString());
            al.add(this.rowlimit.toString());
            al.add(this.nature);
            al.add(this.test);
            al.add(this.testcase);
            al.add(this.country);
            al.add(this.property);

            this.db.connect();
            this.db.update(sql.toString(), al);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.db.disconnect();
        }
// System.out.println ( "SQL : " + sql.toString ( ) ) ;
    }
}
