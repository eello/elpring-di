package eello.elpring.di.beans.factory;

import eello.elpring.di.exception.BeansException;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface ListableBeanFactory extends BeanFactory {

    boolean containsBeanDefinition(String beanName);
    int getBeanDefinitionCount();
    String[] getBeanDefinitionNames();
    Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;
    String[] getBeanNamesForType(Class<?> type);
    <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;
}
