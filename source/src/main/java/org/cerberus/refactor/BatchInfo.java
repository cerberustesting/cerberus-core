/*
 * Cerberus  Copyright (C) 2013  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.refactor;

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
