package org.cerberus.util.observe;

/**
 * An observable-pattern's observer
 *
 * @param <TOPIC> the associated topic of received item from {@link Observable} triggers
 * @param <ITEM>  the type of item that will be received from {@link Observable} triggers
 * @author Aurelien Bourdon
 */
public interface Observer<TOPIC, ITEM> {

    /**
     * Callback method when a create action from an {@link Observable} occurs
     *
     * @param topic the associated topic
     * @param item  the created item
     */
    void observeCreate(TOPIC topic, ITEM item);

    /**
     * Callback method when an update action from an {@link Observable} occurs
     *
     * @param topic the associated topic
     * @param item  the updated item
     */
    void observeUpdate(TOPIC topic, ITEM item);

    /**
     * Callback method when a delete action from an {@link Observable} occurs
     *
     * @param topic the associated topic
     * @param item  the deleted item
     */
    void observeDelete(TOPIC topic, ITEM item);

}
