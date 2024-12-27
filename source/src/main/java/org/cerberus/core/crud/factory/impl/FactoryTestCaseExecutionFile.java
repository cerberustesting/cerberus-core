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
package org.cerberus.core.crud.factory.impl;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.TestCaseExecutionFile;
import org.cerberus.core.crud.factory.IFactoryTestCaseExecutionFile;
import org.springframework.stereotype.Service;

/**
 * @author vertigo
 */
@Service
public class FactoryTestCaseExecutionFile implements IFactoryTestCaseExecutionFile {

    @Override
    public TestCaseExecutionFile create(long id, long exeId, String level
            , String fileDesc, String fileName, String fileType
            , String usrCreated, Timestamp dateCreated, String usrModif
            , Timestamp dateModif) {
        TestCaseExecutionFile newObject = new TestCaseExecutionFile();
        newObject.setId(id);
        newObject.setExeId(exeId);
        newObject.setLevel(level);
        newObject.setFileDesc(fileDesc);
        newObject.setFileName(fileName);
        newObject.setFileType(fileType);
        newObject.setUsrCreated(usrCreated);
        newObject.setDateCreated(dateCreated);
        newObject.setUsrModif(usrModif);
        newObject.setDateModif(dateModif);
        return newObject;
    }


}
