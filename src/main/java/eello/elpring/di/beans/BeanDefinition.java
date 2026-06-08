package eello.elpring.di.beans;

import java.lang.reflect.Constructor;

public interface BeanDefinition {

	String getBeanClassName();
	Class<?> getBeanType();
	String getBeanTypeName();
	Class<?>[] getInterfaceTypes();
	Constructor<?> getConstructors();
	Class<?>[] getDependsOn();
	boolean isPrimary();
	boolean isLazyInit();
	BeanScope getScope();
	boolean equals(Object o);
	int hashCode();
}
