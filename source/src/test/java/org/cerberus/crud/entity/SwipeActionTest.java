package org.cerberus.crud.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * Set of unit tests for the {@link SwipeAction} class
 *
 * @author Aurelien Bourdon.
 */
public class SwipeActionTest {

    @Test
    public void testCreateCommonAction() throws Exception {
        SwipeAction action = SwipeAction.fromStrings("UP", null);
        Assert.assertEquals(SwipeAction.ActionType.UP, action.getActionType());
        Assert.assertFalse(action.isCustom());
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateCommonActionHaveNotCustomDirection() throws Exception {
        SwipeAction.fromStrings("UP", null).getCustomDirection();
    }

    @Test
    public void testCreateCustomAction() throws Exception {
        SwipeAction action = SwipeAction.fromStrings("CUSTOM", "0;1;2;3");
        Assert.assertEquals(SwipeAction.ActionType.CUSTOM, action.getActionType());
        Assert.assertTrue(action.isCustom());
        Assert.assertEquals(0, action.getCustomDirection().getX1());
        Assert.assertEquals(1, action.getCustomDirection().getY1());
        Assert.assertEquals(2, action.getCustomDirection().getX2());
        Assert.assertEquals(3, action.getCustomDirection().getY2());
    }

    @Test(expected = SwipeAction.SwipeActionException.class)
    public void testCreateNotValidAction() throws Exception {
        SwipeAction.fromStrings("UNKNOWN", null);
    }

    @Test(expected = SwipeAction.SwipeActionException.class)
    public void testCreateCustomActionWithNullDirection() throws Exception {
        SwipeAction.fromStrings("CUSTOM", null);
    }

    @Test(expected = SwipeAction.SwipeActionException.class)
    public void testCreateCustomActionWithInvalidDirection() throws Exception {
        SwipeAction.fromStrings("CUSTOM", "wrong");
    }

}
