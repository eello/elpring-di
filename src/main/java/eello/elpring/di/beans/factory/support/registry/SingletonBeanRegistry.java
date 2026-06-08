package eello.elpring.di.beans.factory.support.registry;

public interface SingletonBeanRegistry {

    void addSingleton(String beanName, Object singletonObject);
    Object getSingleton(String beanName);
    Object[] getAllBeans();
    void setCurrentlyInCreation(String beanName);
    void completeCurrentlyInCreation(String beanName);
    boolean isSingletonCurrentlyInCreation(String beanName);
}
