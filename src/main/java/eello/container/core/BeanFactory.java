package eello.container.core;

import java.lang.reflect.InvocationTargetException;

public interface BeanFactory {

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
