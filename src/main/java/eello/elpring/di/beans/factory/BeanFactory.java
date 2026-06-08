package eello.elpring.di.beans.factory;

import eello.elpring.di.exception.BeansException;

public interface BeanFactory {

    Object getBean(String name) throws BeansException;
    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
    <T> T getBean(Class<T> requiredType) throws BeansException;
}
