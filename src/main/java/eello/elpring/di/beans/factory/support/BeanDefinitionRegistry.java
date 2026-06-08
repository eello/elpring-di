package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.BeanDefinition;

public interface BeanDefinitionRegistry {

    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
    BeanDefinition getBeanDefinition(String beanName);
    String[] getBeanDefinitionNames();
    int getBeanDefinitionCount();
}
