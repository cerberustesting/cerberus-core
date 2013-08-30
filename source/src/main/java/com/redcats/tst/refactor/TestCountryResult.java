package com.redcats.tst.refactor;

public class TestCountryResult {

    private Country country;
    private String date;
    private Integer id;
    private Boolean oK;
    private Test test;

    public Country getCountry() {

        return this.country;
    }

    public String getDate() {

        return this.date;
    }

    public Integer getId() {

        return this.id;
    }

    public Boolean getoK() {

        return this.oK;
    }

    public Test getTest() {

        return this.test;
    }

    public void setCountry(Country country) {

        this.country = country;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    public void setoK(Boolean oK) {

        this.oK = oK;
    }

    public void setoK(String oK) {

        if (oK.compareTo("OK") == 0) {
            this.oK = true;
            // System.out.println ( oK + " True " ) ;
        } else {
            // System.out.println ( oK + " False " ) ;
            this.oK = false;
        }
    }

    public void setTest(Test test) {

        this.test = test;
    }

    public void updateCountryStatistics() {

        if (this.country != null && this.test != null) {
            // System.out.println("Adding available for : "
            // + this.country.getName() + " in test -> "
            // + this.getTest().getTest());
            this.country.addAvailableTest();
        }

    }

    public void updateExecutionStatistics() {

        if (this.country != null && this.test != null) {
            this.country.addExecutedTest();

            if (this.oK) {
                this.country.addOKTest();
            } else {
                this.country.addKOTest();
            }
        }
    }
}
