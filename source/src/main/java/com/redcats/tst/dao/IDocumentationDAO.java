/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redcats.tst.dao;

import com.redcats.tst.entity.Documentation;

/**
 *
 * @author bcivel
 */
public interface IDocumentationDAO {

    Documentation findDocumentationByKey(String docTable, String docField);
}
