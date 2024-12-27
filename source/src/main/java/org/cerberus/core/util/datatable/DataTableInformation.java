/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.util.datatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.cerberus.core.util.ParameterParserUtil;

/**
 *
 * @author bcivel
 */
public class DataTableInformation {

    int startPosition;
    int length;
    String searchParameter;
    int columnToSortParameter;
    String sColumns;
    String columnToSort[];
    String columnName;
    String sort;
    List<String> individualLike;
    Map<String, List<String>> individualSearch;

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSearchParameter() {
        return searchParameter;
    }

    public void setSearchParameter(String searchParameter) {
        this.searchParameter = searchParameter;
    }

    public int getColumnToSortParameter() {
        return columnToSortParameter;
    }

    public void setColumnToSortParameter(int columnToSortParameter) {
        this.columnToSortParameter = columnToSortParameter;
    }

    public String getsColumns() {
        return sColumns;
    }

    public void setsColumns(String sColumns) {
        this.sColumns = sColumns;
    }

    public String getSort() {
        return sort;
    }

    public String[] getColumnToSort() {
        return columnToSort;
    }

    public void setColumnToSort(String[] columnToSort) {
        this.columnToSort = columnToSort;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public void setSort(String sort) {
        this.sort = sort;
    }

    public List<String> getIndividualLike() {
        return individualLike;
    }

    public void setIndividualLike(List<String> individualLike) {
        this.individualLike = individualLike;
    }

    public Map<String, List<String>> getIndividualSearch() {
        return individualSearch;
    }

    public void setIndividualSearch(Map<String, List<String>> individualSearch) {
        this.individualSearch = individualSearch;
    }

    
    
    public DataTableInformation(HttpServletRequest request, String defaultColumns) {
        parseDataTableInformation(request, defaultColumns);
    }

    /**
     *
     * @param request the request
     */
    private void parseDataTableInformation(HttpServletRequest request, String defaultColumns) {

        this.setStartPosition(Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayStart"), "0")));
        this.setLength(Integer.valueOf(ParameterParserUtil.parseStringParam(request.getParameter("iDisplayLength"), "10")));
        this.setSearchParameter(ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), ""));
        this.setColumnToSortParameter(Integer.parseInt(ParameterParserUtil.parseStringParam(request.getParameter("iSortCol_0"), "0")));
        this.setsColumns(ParameterParserUtil.parseStringParam(request.getParameter("sColumns"), defaultColumns));
        this.setSort(ParameterParserUtil.parseStringParam(request.getParameter("sSortDir_0"), "asc"));
        this.setColumnToSort(sColumns.split(","));
        this.setColumnName(columnToSort[columnToSortParameter]);
        this.setIndividualLike(new ArrayList<>(Arrays.asList(ParameterParserUtil.parseStringParam(request.getParameter("sLike"), "").split(","))));
    
        Map<String, List<String>> individualSearch = new HashMap<>();
        for (int a = 0; a < columnToSort.length; a++) {
            if (null != request.getParameter("sSearch_" + a) && !request.getParameter("sSearch_" + a).isEmpty()) {
                List<String> search = new ArrayList<>(Arrays.asList(request.getParameter("sSearch_" + a).split(",")));
                if (individualLike.contains(columnToSort[a])) {
                    individualSearch.put(columnToSort[a] + ":like", search);
                } else {
                    individualSearch.put(columnToSort[a], search);
                }

            }
        }
        this.setIndividualSearch(individualSearch);
    }

}
