package eello.elpring.di.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface BeanDefinition {

	String getBeanClassName();
	Class<?> getBeanType();
	String getBeanTypeName();
	Class<?>[] getInterfaceTypes();
	Constructor<?> getConstructors();
	Parameter[] getDependsOn();
	Class<? extends Annotation>[] getMetaAnnotations();
	boolean hasAnnotation(Class<? extends Annotation> annotation);
	boolean isPrimary();
	boolean isLazyInit();
	BeanScope getScope();
	boolean equals(Object o);
	int hashCode();
	String getFactoryBeanName();
	Method getFactoryMethod();
	boolean isConfigurationClass();
	boolean isFactoryBeanMethod();
}
