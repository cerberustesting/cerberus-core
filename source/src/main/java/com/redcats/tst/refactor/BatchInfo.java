package com.redcats.tst.refactor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BatchInfo {

    private String id;
    private String incIni;
    private Integer unit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIncIni() {
        return incIni;
    }

    public void setIncIni(String incIni) {
        this.incIni = incIni;
    }

    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public String calculateString(String original, Integer times) {
        try {
            times = times + 1;
            Integer number = Integer.parseInt(this.incIni) * times;
            Integer result = Integer.parseInt(original) + number;
            if (times < 10) {
                return "0" + result.toString();
            }
            return result.toString();
        } catch (NullPointerException e) {
            System.out.println("Null value, BatchInfo not properly filled.");

        } catch (NumberFormatException e) {
            System.out.println("Could not understand the number on the database.");

        }

        return original;
    }

    public String calculateBatNumExe(ResultSet rs, Integer count,
            String batNumExe) {
        try {
            if (rs.next()) {
                count = Integer.parseInt(rs.getString(1));
            }

        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this.calculateString(batNumExe, count);
    }
}
