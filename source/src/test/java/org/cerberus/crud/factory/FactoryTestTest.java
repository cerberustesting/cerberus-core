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
package org.cerberus.crud.factory;

import org.cerberus.crud.factory.impl.FactoryTest;
import org.cerberus.crud.service.IParameterService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * {Insert class description here}
 *
 * @author Benoit CIVEL
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class FactoryTestTest {

    @InjectMocks
    private FactoryTest factory;

    @Test
    public void testCanCreateTest() {

        org.cerberus.crud.entity.Test test;
        String tst = "test";
        String description = "description";
        String active = "Y";
        String automated = "N";
        String tDateCrea = "tDateCrea";

        test = factory.create(tst, description, active, automated, tDateCrea);

        assertEquals(test.getTest(), tst);
        assertEquals(test.getDescription(), description);
        assertEquals(test.getActive(), active);
        assertEquals(test.getAutomated(), automated);
        assertEquals(test.gettDateCrea(), tDateCrea);
    }
}
