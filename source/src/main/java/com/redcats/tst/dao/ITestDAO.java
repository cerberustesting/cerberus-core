package com.redcats.tst.dao;

import com.redcats.tst.entity.Test;
import java.util.List;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 19/Dez/2012
 * @since 2.0.0
 */
public interface ITestDAO {

    List<Test> findAllTest();
}
