package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.BeanDefinitionHolder;
import eello.elpring.di.annotation.Component;
import eello.elpring.di.beans.DefaultBeanDefinition;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * basePackages 경로 안에 있는 모든 클래스 중 빈 등록 대상의 클래스들을
 * BeanDefinition 으로 만들고 BeanDefinitionRegistry 에 등록
 */
public class ClassPathBeanDefinitionScanner {

	private final ClassLoader cl;
	private final BeanDefinitionRegistry registry;

	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this.cl = Thread.currentThread().getContextClassLoader();
		this.registry = registry;
	}

	/*
		새로 스캔한 BeanDefinition 수 리턴
	 */
	public int scan(String... basePackage) throws ClassNotFoundException {
		int beanCountAtScanStart = registry.getBeanDefinitionCount();
		this.doScan(basePackage);
		return registry.getBeanDefinitionCount() - beanCountAtScanStart;
	}

	/*
		모든 basePackage 들을 읽어 후보 빈 정의(BeanDefinition)을 만들어
		BeanDefinitionRegistry(DefaultListableBeanFactory) 에 등록
	 */
	protected Set<BeanDefinition> doScan(String... basePackages) throws ClassNotFoundException {
		Set<BeanDefinition> beanDefinitions = new HashSet<>();
		for (String basePackage : basePackages) {
			Set<BeanDefinition> candidateComponents = findCandidateComponents(basePackage);
			beanDefinitions.addAll(candidateComponents);
		}

		for (BeanDefinition candidateDefinition : beanDefinitions) {
			String beanName = generateBeanName(candidateDefinition);
			BeanDefinitionHolder candidateDefinitionHolder = new BeanDefinitionHolder(beanName, candidateDefinition);
			BeanDefinitionReaderUtils.registerBeanDefinition(candidateDefinitionHolder, registry);
		}

		return beanDefinitions;
	}

	private String generateBeanName(BeanDefinition beanDefinition) {
		Class<?> beanClass = beanDefinition.getBeanType();
		Component component = beanClass.getAnnotation(Component.class);

		String beanName = component.value();
		if (!beanName.isBlank()) {
			return beanName;
		}

		char[] defaultBeanName = beanDefinition.getBeanClassName().toCharArray();
		defaultBeanName[0] = Character.toLowerCase(defaultBeanName[0]);
		return new String(defaultBeanName);
	}

	private Set<BeanDefinition> findCandidateComponents(String basePackage) throws ClassNotFoundException {
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
