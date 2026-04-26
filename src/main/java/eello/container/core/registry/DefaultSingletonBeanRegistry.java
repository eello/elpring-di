package eello.container.core.registry;

import java.util.HashMap;
import java.util.Map;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    private Map<String, Object> singletons = new HashMap<>();

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
}
