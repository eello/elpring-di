package eello.container.core;

import java.lang.reflect.Constructor;

public interface BeanDefinition {

	String getBeanName();
	Class<?> getBeanType();
	String getBeanTypeName();
	Class<?>[] getInterfaceTypes();
	Constructor<?> getConstructors();
	Class<?>[] getDependsOn();
	boolean isPrimary();
	BeanScope getScope();
}
