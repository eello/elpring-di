package eello.container;

import eello.container.annotation.Component;
import eello.container.core.BeanDefinition;
import eello.container.core.DefaultBeanDefinition;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ClassPathBeanDefinitionScanner {

	private ClassLoader cl;

	public ClassPathBeanDefinitionScanner() {
		this.cl = Thread.currentThread().getContextClassLoader();
	}

	public Set<BeanDefinition> doScan(String basePackage) throws ClassNotFoundException {
		Set<Class<?>> classes = findAllClasses(basePackage);
		Set<BeanDefinition> beanDefinitions = new HashSet<>();

		for (Class<?> clazz : classes) {
			if (!clazz.isAnnotation() && isComponent(clazz)) {
				beanDefinitions.add(DefaultBeanDefinition.of(clazz));
			}
		}

		return beanDefinitions;
	}

	private Set<Class<?>> findAllClasses(String basePackage) throws ClassNotFoundException {
		Set<Class<?>> classes = new HashSet<>();

		try {
			String path = basePackage.replace(".", "/");
			URL resource = cl.getResource(path);
			File directory = new File(resource.toURI());

			for (File file : directory.listFiles()) {
				if (file.isDirectory()) {
					String childPackageName = basePackage + "." + file.getName();
					classes.addAll(findAllClasses(childPackageName));
				} else if (file.getName().endsWith(".class")) {
					String className = basePackage + '.' + file.getName().substring(0, file.getName().length() - 6);
					classes.add(Class.forName(className));
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return classes;
	}

	// clazz 에 적용된 어노테이션들을 재귀적으로 검사해 @Component 가 적용된 경우 true 리턴
	private boolean isComponent(Class<?> clazz) {
		if (clazz.isAnnotationPresent(Component.class)) return true;

		for (Annotation annotation : clazz.getAnnotations()) {
			if (isSystemAnnotation(annotation)) continue;
			if (isComponent(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}

	private boolean isSystemAnnotation(Annotation annotation) {
		String packageName = annotation.annotationType().getPackageName();
		return packageName.startsWith("java.lang.annotation") ||
				packageName.startsWith("jakarta.annotation");
	}
}
