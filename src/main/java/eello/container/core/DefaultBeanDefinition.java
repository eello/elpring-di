package eello.container.core;

import eello.container.annotation.Primary;

import java.lang.reflect.Constructor;

public class DefaultBeanDefinition implements BeanDefinition {

	private String beanName;
	private Class<?> beanType;
	private Class<?>[] interfaces;
	private Constructor<?> constructor;
	private Class<?>[] dependsOn;
	private boolean primary;
	private BeanScope scope;

	private DefaultBeanDefinition() {}

	public static BeanDefinition of(Class<?> clazz) {
		return of(clazz, null);
	}

	public static BeanDefinition of(Class<?> clazz, String beanName) {
		DefaultBeanDefinition def = new DefaultBeanDefinition();
		def.beanName = beanName != null ? beanName : DefaultBeanNameResolver.resolve(clazz.getSimpleName());
		def.beanType = clazz;
		def.interfaces = clazz.getInterfaces();
		def.constructor = clazz.getConstructors()[0];
		def.dependsOn = def.constructor.getParameterTypes();
		def.primary = clazz.isAnnotationPresent(Primary.class);
		def.scope = BeanScope.SINGLETON;
		return def;
	}

	@Override
	public String getBeanName() {
		return beanName;
	}

	@Override
	public Class<?> getBeanType() {
		return beanType;
	}

	@Override
	public String getBeanTypeName() {
		return beanType.getName();
	}

	@Override
	public Class<?>[] getInterfaceTypes() {
		return interfaces;
	}

	@Override
	public Constructor<?> getConstructors() {
		return constructor;
	}

	@Override
	public Class<?>[] getDependsOn() {
		return dependsOn;
	}

	@Override
	public boolean isPrimary() {
		return primary;
	}

	@Override
	public BeanScope getScope() {
		return scope;
	}

	private static class DefaultBeanNameResolver {

		public static String resolve(String simpleClassName) {
			char[] charArray = simpleClassName.toCharArray();
			charArray[0] = Character.toLowerCase(charArray[0]);
			return new String(charArray);
		}
	}
}
