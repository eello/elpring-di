package eello.container.core;

import java.lang.reflect.InvocationTargetException;

public interface BeanInitializer {

	void initialize(String basePackage) throws
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException, ClassNotFoundException;
}
