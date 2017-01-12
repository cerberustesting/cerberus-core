package org.cerberus.util.observe;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ObservableEngine} unit tests
 */
public class ObservableEngineTest {

    /**
     * A dummy observer that count the number of change calls from an {@link Observable}
     */
    private static class Counter implements Observer<String, String> {

        private int creates = 0;

        private int updates = 0;

        private int deletes = 0;

        @Override
        public void observeCreate(String topic, String s) {
            creates++;
        }

        @Override
        public void observeUpdate(String topic, String change) {
            updates++;
        }

        @Override
        public void observeDelete(String topic, String s) {
            deletes++;
        }

        public int getCreates() {
            return creates;
        }

        public int getUpdates() {
            return updates;
        }

        public int getDeletes() {
            return deletes;
        }

    }

    /**
     * A dummy topic
     */
    private static final String TOPIC = "topic";

    /**
     * A dummy observable item
     */
    private static final String ITEM = "item";

    /**
     * The tested {@link ObservableEngine}
     */
    private ObservableEngine<String, String> observableEngine;

    @Before
    public void setUp() {
        observableEngine = new ObservableEngine<>();
    }

    @Test
    public void testAllWithFullObserver() {
        Counter counter = new Counter();
        Assert.assertEquals(0, counter.getCreates());
        Assert.assertEquals(0, counter.getUpdates());
        Assert.assertEquals(0, counter.getDeletes());

        observableEngine.register(counter);
        observableEngine.fireCreate(TOPIC, ITEM);
        observableEngine.fireUpdate(TOPIC, ITEM);
        observableEngine.fireDelete(TOPIC, ITEM);
        Assert.assertEquals(1, counter.getCreates());
        Assert.assertEquals(1, counter.getUpdates());
        Assert.assertEquals(1, counter.getDeletes());

        observableEngine.unregister(counter);
        observableEngine.fireCreate(TOPIC, ITEM);
        observableEngine.fireUpdate(TOPIC, ITEM);
        observableEngine.fireDelete(TOPIC, ITEM);
        Assert.assertEquals(1, counter.getCreates());
        Assert.assertEquals(1, counter.getUpdates());
        Assert.assertEquals(1, counter.getDeletes());
    }

    @Test
    public void testAllWithTopicObserver() {
        Counter counter = new Counter();
        Assert.assertEquals(0, counter.getUpdates());
        Assert.assertEquals(0, counter.getDeletes());

        observableEngine.register(TOPIC, counter);
        observableEngine.fireCreate(TOPIC, ITEM);
        observableEngine.fireUpdate(TOPIC, ITEM);
        observableEngine.fireDelete(TOPIC, ITEM);
        Assert.assertEquals(1, counter.getCreates());
        Assert.assertEquals(1, counter.getUpdates());
        Assert.assertEquals(1, counter.getDeletes());

        observableEngine.unregister(TOPIC, counter);
        observableEngine.fireCreate(TOPIC, ITEM);
        observableEngine.fireUpdate(TOPIC, ITEM);
        observableEngine.fireDelete(TOPIC, ITEM);
        Assert.assertEquals(1, counter.getCreates());
        Assert.assertEquals(1, counter.getUpdates());
        Assert.assertEquals(1, counter.getDeletes());
    }

    @Test
    public void testUnegisterFromAllImpliesUnRegisterFromAny() {
        Counter counter = new Counter();
        Assert.assertEquals(0, counter.getUpdates());

        observableEngine.register(counter);
        observableEngine.register(TOPIC, counter);
        observableEngine.fireUpdate(TOPIC, ITEM);
        Assert.assertEquals(1, counter.getUpdates());

        observableEngine.unregister(counter);
        observableEngine.fireUpdate(TOPIC, ITEM);
        Assert.assertEquals(1, counter.getUpdates());
    }

    @Test
    public void testUnegisterFromAnyDoesNotImpliesUnRegisterFromAll() {
        Counter counter = new Counter();
        Assert.assertEquals(0, counter.getUpdates());

        observableEngine.register(counter);
        observableEngine.register(TOPIC, counter);
        observableEngine.fireUpdate(TOPIC, ITEM);
        Assert.assertEquals(1, counter.getUpdates());

        observableEngine.unregister(TOPIC, counter);
        observableEngine.fireUpdate(TOPIC, ITEM);
        Assert.assertEquals(2, counter.getUpdates());
    }

}
