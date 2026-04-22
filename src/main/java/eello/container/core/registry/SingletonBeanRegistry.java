package eello.container.core.registry;

public interface SingletonBeanRegistry {

    void addSingleton(String beanName, Object singletonObject);
    Object getSingleton(String beanName);
}
