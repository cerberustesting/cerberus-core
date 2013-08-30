package com.redcats.tst.entity;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/Dez/2012
 * @since 2.0.0
 */
public class Country {
    /**
     * Name of Country (ex. Portugal, Belgium, etc.).
     */
    private String country;
    /**
     * Code of Country (ex. PT, BE, etc.).
     */
    private String countryCode;

    public Country(String tempCountryCode) {
        this.countryCode = tempCountryCode;
    }

    public Country(String tempCountry, String tempCountryCode) {
        this.country = tempCountry;
        this.countryCode = tempCountryCode;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String tempCountry) {
        this.country = tempCountry;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String tempCountryCode) {
        this.countryCode = tempCountryCode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Country country1 = (Country) object;

        if (!this.country.equals(country1.country)) return false;
        if (!this.countryCode.equals(country1.countryCode)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.country.hashCode();
        result = 31 * result + this.countryCode.hashCode();
        return result;
    }
}
