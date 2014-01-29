/* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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

package org.cerberus.service.impl;

import java.util.List;
import org.cerberus.dao.IZZTestDataDAO;
import org.cerberus.entity.ZZTestData;
import org.cerberus.exception.CerberusException;
import org.cerberus.service.IZZTestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ZZTestDataService implements IZZTestDataService {

    @Autowired
    IZZTestDataDAO zzTestDataDAO;
    
    @Override
    public void createZZTestData(ZZTestData testData) throws CerberusException {
        zzTestDataDAO.createZZTestData(testData);
    }

    @Override
    public void updateZZTestData(ZZTestData testData) throws CerberusException {
        zzTestDataDAO.updateZZTestData(testData);
    }

    @Override
    public void deleteZZTestData(ZZTestData testData) throws CerberusException {
        zzTestDataDAO.deleteZZTestData(testData);
    }

    @Override
    public List<ZZTestData> findAllZZTestData() {
        return zzTestDataDAO.findAllZZTestData();
    }

    @Override
    public List<ZZTestData> findZZTestDataListByCriteria(int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        return zzTestDataDAO.findZZTestDataListByCriteria(start, amount, column, dir, searchTerm, individualSearch);
    }
    
}
