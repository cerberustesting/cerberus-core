package org.cerberus.util.observe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple ready-to-go delegate class to handle the {@link Observable} contract
 *
 * @author Aurelien Bourdon
 */
public class ObservableEngine<TOPIC, ITEM> implements Observable<TOPIC, ITEM> {

    private Set<Observer<TOPIC, ITEM>> fullObservers = new HashSet<>();

    private Map<TOPIC, Set<Observer<TOPIC, ITEM>>> topicObservers = new HashMap<>();
    private Map<Observer<TOPIC, ITEM>, Set<TOPIC>> reverseTopicObservers = new HashMap<>();

    @Override
    public boolean register(Observer<TOPIC, ITEM> observer) {
        synchronized (fullObservers) {
            return fullObservers.add(observer);
        }
    }

    @Override
    public boolean register(TOPIC topic, Observer<TOPIC, ITEM> observer) {
        synchronized (topicObservers) {
            Set<Observer<TOPIC, ITEM>> observers = topicObservers.get(topic);
            if (observers == null) {
                observers = new HashSet<>();
                topicObservers.put(topic, observers);
            }
            Set<TOPIC> topics = reverseTopicObservers.get(observer);
            if (topics == null) {
                topics = new HashSet<>();
            }
            topics.add(topic);
            reverseTopicObservers.put(observer, topics);
            return observers.add(observer);
        }
    }

    @Override
    public boolean unregister(TOPIC topic, Observer<TOPIC, ITEM> observer) {
        synchronized (topicObservers) {
            Set<Observer<TOPIC, ITEM>> observers = topicObservers.get(topic);
            if (observers == null) {
                return false;
            }
            return observers.remove(observer);
        }
    }

    @Override
    public boolean unregister(Observer<TOPIC, ITEM> observer) {
        boolean success = false;
        synchronized (fullObservers) {
            success |= fullObservers.remove(observer);
        }
        synchronized (topicObservers) {
            Set<TOPIC> topics = reverseTopicObservers.get(observer);
            if (topics != null) {
                for (TOPIC topic : topics) {
                    success |= topicObservers.get(topic).remove(observer);
                }
            }
        }
        return success;
    }

    @Override
    public void fireCreate(TOPIC topic, ITEM item) {
        for (Observer<TOPIC, ITEM> observer : observersToTrigger(topic)) {
            observer.observeCreate(topic, item);
        }
    }

    @Override
    public void fireUpdate(TOPIC topic, ITEM item) {
        for (Observer<TOPIC, ITEM> observer : observersToTrigger(topic)) {
            observer.observeUpdate(topic, item);
        }
    }

    @Override
    public void fireDelete(TOPIC topic, ITEM item) {
        for (Observer<TOPIC, ITEM> observer : observersToTrigger(topic)) {
            observer.observeDelete(topic, item);
        }
    }

    private Set<Observer<TOPIC, ITEM>> observersToTrigger(TOPIC topic) {
        Set<Observer<TOPIC, ITEM>> selectedObservers;
        synchronized (fullObservers) {
            selectedObservers = new HashSet<>(fullObservers);
        }
        Set<Observer<TOPIC, ITEM>> selectedTopicObservers = null;
        synchronized (topicObservers) {
            if (topicObservers.containsKey(topic)) {
                selectedTopicObservers = new HashSet<>(topicObservers.get(topic));
            }
        }
        if (selectedTopicObservers != null) {
            selectedObservers.addAll(selectedTopicObservers);
        }
        return selectedObservers;
    }

}
