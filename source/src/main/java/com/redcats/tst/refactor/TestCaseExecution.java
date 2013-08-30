package com.redcats.tst.refactor;

import com.redcats.tst.database.DatabaseSpring;
import java.util.ArrayList;

public class TestCaseExecution {

    private String application;
    private String browser;
    private String build;
    private String controlstatus;
    private String country;
    private final DatabaseSpring db;
    private String end;
    private String environment;
    private String ip;
    private String port;
    private String revision;
    private String start;
    private String test;
    private String testcase;
    private String verbose;
    private String url;
    private String status;

    public TestCaseExecution() {

        this.db = new DatabaseSpring();
    }

    public String getApplication() {

        return this.application;
    }

    public String getBrowser() {

        return this.browser;
    }

    public String getBuild() {

        return this.build;
    }

    public String getControlstatus() {

        return this.controlstatus;
    }

    public String getCountry() {

        return this.country;
    }

    public String getEnd() {

        return this.end;
    }

    public String getEnvironment() {

        return this.environment;
    }

    public String getIp() {

        return this.ip;
    }

    public String getPort() {

        return this.port;
    }

    public String getRevision() {

        return this.revision;
    }

    public String getStart() {

        return this.start;
    }

    public String getTest() {

        return this.test;
    }

    public String getTestcase() {

        return this.testcase;
    }

    public String getVerbose() {

        return this.verbose;
    }

    public String getUrl() {

        return this.url;
    }

    public String getStatus() {
        return this.status;
    }

    public void insert() {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO `testcaseexecution` (`ID`,`Test`,`TestCase`,`Build`,`Revision`,`Environment`,`Country`,`Browser`,`Start`,`End`,`ControlStatus`,`Application`,`IP`,`URL`,`Port`,`Verbose`, `Status`) VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        try {
            ArrayList<String> al = new ArrayList<String>();
            al.add("0");
            al.add(this.test);
            al.add(this.testcase);
            al.add(this.build);
            al.add(this.revision);
            al.add(this.environment);
            al.add(this.country);
            al.add(this.browser);
            al.add(this.start);
            al.add(this.end);
            al.add(this.controlstatus);
            al.add(this.application);
            al.add(this.ip);
            al.add(this.url);
            al.add(this.port);
            al.add(this.verbose);
            al.add(this.status);

            this.db.connect();
            this.db.update(sql.toString(), al);
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            this.db.disconnect();
        }

    }

    public void setApplication(String application) {

        this.application = application;
    }

    public void setBrowser(String browser) {

        this.browser = browser;
    }

    public void setBuild(String build) {

        this.build = build;
    }

    public void setControlstatus(String controlstatus) {

        this.controlstatus = controlstatus;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public void setEnd(String end) {

        this.end = end;
    }

    public void setEnvironment(String environment) {

        this.environment = environment;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }

    public void setPort(String port) {

        this.port = port;
    }

    public void setRevision(String revision) {

        this.revision = revision;
    }

    public void setStart(String start) {

        this.start = start;
    }

    public void setTest(String test) {

        this.test = test;
    }

    public void setTestcase(String testcase) {

        this.testcase = testcase;
    }

    public void setVerbose(String verbose) {

        this.test = verbose;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public void setStatus(String tempStatus) {
        this.status = tempStatus;
    }
}
