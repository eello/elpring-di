package eello.elpring.di.beans.factory;

import eello.elpring.di.beans.BeanDefinition;

import java.lang.reflect.InvocationTargetException;

public interface LegacyBeanFactory {

	void registerBean(BeanDefinition def) throws
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException;
	Object getBean(String beanName);
	<T> T getBean(String beanName, Class<T> requiredType);
	<T> T[] getBean(Class<T> requiredType);
	boolean isRegistered(String beanName);
	boolean isRegistered(BeanDefinition def);
}
