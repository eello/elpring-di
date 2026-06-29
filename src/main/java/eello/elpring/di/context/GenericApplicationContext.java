package eello.elpring.di.context;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.factory.support.DefaultListableBeanFactory;
import eello.elpring.di.beans.factory.support.BeanDefinitionRegistry;
import eello.elpring.di.exception.BeansException;

import java.util.Map;

public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

    private final DefaultListableBeanFactory beanFactory;
    private final ClassLoader cl = Thread.currentThread().getContextClassLoader();

    public GenericApplicationContext() {
        this(new DefaultListableBeanFactory());
    }

    public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.beanFactory.setApplicationContext(this);
    }

    @Override
    public DefaultListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return null;
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanFactory.getBeanDefinitionNames();
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanFactory.getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return this.beanFactory.getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(eello.elpring.di.inbox.ResolvableType type) {
        return this.beanFactory.getBeanNamesForType(type);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return this.beanFactory.getBeansOfType(type);
    }

    @Override
    public ClassLoader getClassLoader() {
        return cl;
    }

    public void registerBean() {
        // TODO 빈 등록 구현
    }
}
