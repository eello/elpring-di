package eello.elpring.di.beans.factory.support.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    private Map<String, Object> singletons = new HashMap<>();
    private Set<String> singletonCurrentlyInCreation = ConcurrentHashMap.newKeySet();

    @Override
    public void addSingleton(String beanName, Object singletonObject) {
        if (singletons.containsKey(beanName)) {
            throw new IllegalStateException("Singleton bean '" + beanName + "' is already present");
        }

        singletons.put(beanName, singletonObject);
    }

    @Override
    public Object getSingleton(String beanName) {
        return singletons.get(beanName);
    }

    @Override
    public Object[] getAllBeans() {
        return singletons.values().toArray();
    }

    @Override
    public void setCurrentlyInCreation(String beanName) {
        singletonCurrentlyInCreation.add(beanName);
    }

    @Override
    public void completeCurrentlyInCreation(String beanName) {
        singletonCurrentlyInCreation.remove(beanName);
    }

    @Override
    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return singletonCurrentlyInCreation.contains(beanName);
    }
}
