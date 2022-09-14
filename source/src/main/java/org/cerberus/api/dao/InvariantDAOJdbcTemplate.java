/*
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

package org.cerberus.api.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.Invariant;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class InvariantDAOJdbcTemplate implements DAO<Invariant> {

    private static final Logger LOG = LogManager.getLogger(InvariantDAOJdbcTemplate.class);
    private final JdbcTemplate jdbcTemplate;
    private final int[] invariantTypes = {
            Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR
    };

    public InvariantDAOJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    RowMapper<Invariant> rowMapper = (rs, rowNum) -> Invariant.builder()
            .idName(rs.getString("idname"))
            .value((rs.getString("value")))
            .sort(rs.getInt("sort"))
            .description(rs.getString("description"))
            .veryShortDesc(rs.getString("VeryShortDesc"))
            .gp1(rs.getString("gp1"))
            .gp2(rs.getString("gp2"))
            .gp3(rs.getString("gp3"))
            .gp4(rs.getString("gp4"))
            .gp5(rs.getString("gp5"))
            .gp6(rs.getString("gp6"))
            .gp7(rs.getString("gp7"))
            .gp8(rs.getString("gp8"))
            .gp9(rs.getString("gp9"))
            .build();

    @Override
    public List<Invariant> list() {
        String sql = "SELECT `idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`, `gp4`, `gp5`, `gp6`, `gp7`, `gp8`, `gp9` FROM `invariant`";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void create(Invariant invariant) {
        String sql = "INSERT INTO invariant (`idname`, `value`, `sort`, `description`, `VeryShortDesc`, `gp1`, `gp2`, `gp3`, `gp4`, `gp5`, `gp6`, `gp7`, `gp8`, `gp9`) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        int insert = jdbcTemplate.update(
                sql,
                new Object[]{
                        invariant.getIdName(),
                        invariant.getValue(),
                        invariant.getSort(),
                        invariant.getDescription(),
                        invariant.getVeryShortDesc(),
                        invariant.getGp1(),
                        invariant.getGp2(),
                        invariant.getGp3(),
                        invariant.getGp4(),
                        invariant.getGp5(),
                        invariant.getGp6(),
                        invariant.getGp7(),
                        invariant.getGp8(),
                        invariant.getGp9()},
                invariantTypes
        );

        if (insert != 1) {
            LOG.debug("failed insert {}", invariant);
        }
    }

    @Override
    public List<Invariant> findByIdName(String idName) {
        final String sql = "SELECT * FROM invariant i  WHERE i.idname = ? ORDER BY sort";
        return jdbcTemplate.query(sql, rowMapper, idName);
    }

    @Override
    public Optional<Invariant> findByKey(String idName, String value) {
        final String sql = "SELECT * FROM invariant i  WHERE i.idname = ? AND i.value = ? ORDER BY sort";
        Optional<Invariant> invariant = Optional.empty();
        try {
            invariant = Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, idName, value));
        } catch (DataAccessException e) {
            LOG.debug("Invariant not found for idName '{}': {}", idName, e.getMessage());
        }
        return invariant;
    }
}
