package eello.elpring.di.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

public interface BeanDefinition {

	String getBeanClassName();
	Class<?> getBeanType();
	String getBeanTypeName();
	Class<?>[] getInterfaceTypes();
	Constructor<?> getConstructors();
	Class<?>[] getDependsOn();
	Class<? extends Annotation>[] getMetaAnnotations();
	boolean hasAnnotation(Class<? extends Annotation> annotation);
	boolean isPrimary();
	boolean isLazyInit();
	BeanScope getScope();
	boolean equals(Object o);
	int hashCode();
}
