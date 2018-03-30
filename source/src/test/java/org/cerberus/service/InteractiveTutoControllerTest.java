package org.cerberus.service;

import static org.mockito.Mockito.*;

import org.cerberus.crud.dao.impl.InteractiveTutoDAO;
import org.cerberus.crud.entity.InteractiveTuto;
import org.cerberus.crud.entity.InteractiveTutoStepType;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.dto.InteractiveTutoDTO;
import org.cerberus.servlet.crud.interactivetuto.InteractiveTutoController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Integration test for interactive tutorial
 * TODO, will be a good idea to use test with sqlitedb and dbunit
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"/applicationContextTest.xml"})
public class InteractiveTutoControllerTest {

    private static final int ID_INTERACTIVE_TUTO_1 = 1;
    private static final String TITLE_INTERACTIVE_TUTO_1 = "libelle1";
    private static final String DESCR_INTERACTIVE_TUTO_1 = "description1";
    private static final String ROLE_INTERACTIVE_TUTO_1 = "Administrator";
    private static final InteractiveTuto INTERACTIVE_TUTO_1 = new InteractiveTuto(ID_INTERACTIVE_TUTO_1, TITLE_INTERACTIVE_TUTO_1, DESCR_INTERACTIVE_TUTO_1, ROLE_INTERACTIVE_TUTO_1, 1, InteractiveTuto.Level.EASY ,null);

    private static final int ID_INTERACTIVE_TUTO_STEP_1 = 1;
    private static final int ID_INTERACTIVE_TUTO_STEP_2 = 2;
    private static final String SELECTOR_INTERACTIVE_TUTO_STEP_1 = "#seleltor1";
    private static final String SELECTOR_INTERACTIVE_TUTO_STEP_2 = "#selector2";
    private static final String DESCR_INTERACTIVE_TUTO_STEP_1 = "descr blabla 1";
    private static final String DESCR_INTERACTIVE_TUTO_STEP_2 = "descr blabla 2";
    private static final String TYPE_INTERACTIVE_TUTO_STEP_1 = "default";
    private static final String TYPE_INTERACTIVE_TUTO_STEP_2 = "general";
    private static final InteractiveTutoStepType TYPEENUM_INTERACTIVE_TUTO_STEP_1 = InteractiveTutoStepType.DEFAULT;
    private static final InteractiveTutoStepType TYPEENUM_INTERACTIVE_TUTO_STEP_2 = InteractiveTutoStepType.GENERAL;

    @Configuration
    @ImportResource("classpath:/applicationContextTest.xml")
    static class Conf {
        @Bean
        public DatabaseSpring databaseSpring() {
            return Mockito.mock(DatabaseSpring.class);
        }
    }

    @Mock
    private InteractiveTutoDAO interactiveTutoDAO;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement statement;
    @Mock
    private ResultSet resultSet;

    @Autowired
    private DatabaseSpring databaseSpring;

    @Autowired
    @InjectMocks
    private InteractiveTutoController interactiveTutoController;


    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(interactiveTutoDAO.getInteractiveTutorial(ID_INTERACTIVE_TUTO_1, true, "fr")).thenReturn(INTERACTIVE_TUTO_1);
        when(databaseSpring.connect()).thenReturn(connection);
        when(databaseSpring.connect()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

    }

    @Test
    public void getInteractiveTutoWithResultTest() throws Exception{
        when(resultSet.first()).thenReturn(true);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        when(resultSet.getInt("id")).
                thenReturn(ID_INTERACTIVE_TUTO_1).
                thenReturn(ID_INTERACTIVE_TUTO_STEP_1).
                thenReturn(ID_INTERACTIVE_TUTO_STEP_2);

        when(resultSet.getString("title")).thenReturn(TITLE_INTERACTIVE_TUTO_1);

        when(resultSet.getString("role")).thenReturn(ROLE_INTERACTIVE_TUTO_1);

        when(resultSet.getString("selector")).
                thenReturn(SELECTOR_INTERACTIVE_TUTO_STEP_1).
                thenReturn(SELECTOR_INTERACTIVE_TUTO_STEP_2);

        when(resultSet.getString("description")).
                thenReturn(DESCR_INTERACTIVE_TUTO_1).
                thenReturn(DESCR_INTERACTIVE_TUTO_STEP_1).
                thenReturn(DESCR_INTERACTIVE_TUTO_STEP_2);

        when(resultSet.getString("type")).
                thenReturn(TYPE_INTERACTIVE_TUTO_STEP_1).
                thenReturn(TYPE_INTERACTIVE_TUTO_STEP_2);

        InteractiveTutoDTO res = interactiveTutoController.getInteractiveTuto(ID_INTERACTIVE_TUTO_1,null).getBody();

        Assert.assertNotNull(res);
        Assert.assertEquals(res.getId(), ID_INTERACTIVE_TUTO_1);
        Assert.assertEquals(res.getTitle(), TITLE_INTERACTIVE_TUTO_1);
        Assert.assertEquals(res.getDescription(), DESCR_INTERACTIVE_TUTO_1);
        Assert.assertEquals(res.getRole(), ROLE_INTERACTIVE_TUTO_1);

        Assert.assertNotNull(res.getSteps());
        Assert.assertEquals(res.getSteps().size(),2);

        Assert.assertEquals(res.getSteps().get(0).getSelectorJquery(),SELECTOR_INTERACTIVE_TUTO_STEP_1);
        Assert.assertEquals(res.getSteps().get(0).getText(),DESCR_INTERACTIVE_TUTO_STEP_1);
        Assert.assertEquals(res.getSteps().get(0).getType(),TYPEENUM_INTERACTIVE_TUTO_STEP_1);

        Assert.assertEquals(res.getSteps().get(1).getSelectorJquery(),SELECTOR_INTERACTIVE_TUTO_STEP_2);
        Assert.assertEquals(res.getSteps().get(1).getText(),DESCR_INTERACTIVE_TUTO_STEP_2);
        Assert.assertEquals(res.getSteps().get(1).getType(),TYPEENUM_INTERACTIVE_TUTO_STEP_2);

        verify(resultSet, atLeast(2)).close();
        verify(connection, atLeast(2)).close();
    }

    @Test
    public void getInteractiveTutoNoResultTest() throws Exception{
        when(resultSet.first()).thenReturn(false);
        InteractiveTutoDTO res = interactiveTutoController.getInteractiveTuto(ID_INTERACTIVE_TUTO_1,null).getBody();

        Assert.assertNull(res);

        verify(resultSet).close();
        verify(connection).close();
    }
}
