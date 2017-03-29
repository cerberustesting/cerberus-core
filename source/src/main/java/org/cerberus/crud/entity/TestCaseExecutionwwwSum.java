/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
package org.cerberus.crud.entity;

/**
 *
 * @author bcivel
 */
public class TestCaseExecutionwwwSum {

    private int id;
    private int tot_nbhits;
    private int tot_tps;
    private int tot_size;
    private int nb_rc2xx;
    private int nb_rc3xx;
    private int nb_rc4xx;
    private int nb_rc5xx;
    private int img_nb;
    private int img_tps;
    private int img_size_tot;
    private int img_size_max;
    private int js_nb;
    private int js_tps;
    private int js_size_tot;
    private int js_size_max;
    private int css_nb;
    private int css_tps;
    private int css_size_tot;
    private int css_size_max;

    public int getCss_nb() {
        return css_nb;
    }

    public void setCss_nb(int css_nb) {
        this.css_nb = css_nb;
    }

    public int getCss_size_max() {
        return css_size_max;
    }

    public void setCss_size_max(int css_size_max) {
        this.css_size_max = css_size_max;
    }

    public int getCss_size_tot() {
        return css_size_tot;
    }

    public void setCss_size_tot(int css_size_tot) {
        this.css_size_tot = css_size_tot;
    }

    public int getCss_tps() {
        return css_tps;
    }

    public void setCss_tps(int css_tps) {
        this.css_tps = css_tps;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImg_nb() {
        return img_nb;
    }

    public void setImg_nb(int img_nb) {
        this.img_nb = img_nb;
    }

    public int getImg_size_max() {
        return img_size_max;
    }

    public void setImg_size_max(int img_size_max) {
        this.img_size_max = img_size_max;
    }

    public int getImg_size_tot() {
        return img_size_tot;
    }

    public void setImg_size_tot(int img_size_tot) {
        this.img_size_tot = img_size_tot;
    }

    public int getImg_tps() {
        return img_tps;
    }

    public void setImg_tps(int img_tps) {
        this.img_tps = img_tps;
    }

    public int getJs_nb() {
        return js_nb;
    }

    public void setJs_nb(int js_nb) {
        this.js_nb = js_nb;
    }

    public int getJs_size_max() {
        return js_size_max;
    }

    public void setJs_size_max(int js_size_max) {
        this.js_size_max = js_size_max;
    }

    public int getJs_size_tot() {
        return js_size_tot;
    }

    public void setJs_size_tot(int js_size_tot) {
        this.js_size_tot = js_size_tot;
    }

    public int getJs_tps() {
        return js_tps;
    }

    public void setJs_tps(int js_tps) {
        this.js_tps = js_tps;
    }

    public int getNb_rc2xx() {
        return nb_rc2xx;
    }

    public void setNb_rc2xx(int nb_rc2xx) {
        this.nb_rc2xx = nb_rc2xx;
    }

    public int getNb_rc3xx() {
        return nb_rc3xx;
    }

    public void setNb_rc3xx(int nb_rc3xx) {
        this.nb_rc3xx = nb_rc3xx;
    }

    public int getNb_rc4xx() {
        return nb_rc4xx;
    }

    public void setNb_rc4xx(int nb_rc4xx) {
        this.nb_rc4xx = nb_rc4xx;
    }

    public int getNb_rc5xx() {
        return nb_rc5xx;
    }

    public void setNb_rc5xx(int nb_rc5xx) {
        this.nb_rc5xx = nb_rc5xx;
    }

    public int getTot_nbhits() {
        return tot_nbhits;
    }

    public void setTot_nbhits(int tot_nbhits) {
        this.tot_nbhits = tot_nbhits;
    }

    public int getTot_size() {
        return tot_size;
    }

    public void setTot_size(int tot_size) {
        this.tot_size = tot_size;
    }

    public int getTot_tps() {
        return tot_tps;
    }

    public void setTot_tps(int tot_tps) {
        this.tot_tps = tot_tps;
    }
}
