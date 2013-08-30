package com.redcats.tst.refactor;

import java.sql.ResultSet;

public interface DatabaseCRUD {

    void importResultSet(ResultSet rs);

    void insert();

    void update();
}
