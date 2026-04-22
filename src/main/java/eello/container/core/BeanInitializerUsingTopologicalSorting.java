package eello.container.core;

import eello.container.ClassPathBeanDefinitionScanner;
import eello.container.exception.NoUniqueBeanDefinitionException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BeanInitializerUsingTopologicalSorting implements BeanInitializer {

	private BeanFactory beanFactory;
	private ClassPathBeanDefinitionScanner bdScanner;

	private Map<Class<?>, Integer> indegree;
	private Map<Class<?>, List<Class<?>>> dependsOnMe;
	private Map<Class<?>, List<BeanDefinition>> beanDefinitionMap;

	public BeanInitializerUsingTopologicalSorting(BeanFactory beanFactory, ClassPathBeanDefinitionScanner bdScanner) {
		this.beanFactory = beanFactory;
		this.bdScanner = bdScanner;
	}

	@Override
	public void initialize(String basePackage) throws
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException, ClassNotFoundException {
		Set<BeanDefinition> bds = bdScanner.doScan(basePackage);
		init(bds);
		checkPrimaryValidation();

		Queue<Class<?>> queue = new ArrayDeque<>();
		for (Map.Entry<Class<?>, Integer> entry : indegree.entrySet()) {
			if (entry.getValue() == 0) {
				queue.add(entry.getKey());
			}
		}

		int created = 0;
		while (!queue.isEmpty()) {
			Class<?> target = queue.poll();
			List<BeanDefinition> defs = beanDefinitionMap.get(target);

			BeanDefinition bd = defs.getFirst();
			if (beanFactory.isRegistered(bd)) {
				continue;
			}

			beanFactory.registerBean(bd);
			for (Class<?> nextClass : getNextClasses(bd)) {
				if (!indegree.containsKey(nextClass)) {
					continue;
				}

				int n = indegree.get(nextClass);
				if (n == 1) {
					queue.add(nextClass);
					indegree.remove(nextClass);
				} else indegree.put(nextClass, n - 1);
			}

			created++;
		}

		if (created != bds.size()) {
			throw new IllegalStateException("순환참조오류 발생 가능성 있음.");
		}
	}

	private void init(Set<BeanDefinition> bds) {
		indegree = new HashMap<>();
		dependsOnMe = new HashMap<>();
		beanDefinitionMap = new HashMap<>();

		for (BeanDefinition bd : bds) {
			putWithInitializeIfNotContainsKey(beanDefinitionMap, bd.getBeanType(), bd);
			for (Class<?> interfaceType : bd.getInterfaceTypes()) {
				putWithInitializeIfNotContainsKey(beanDefinitionMap, interfaceType, bd);
			}

			putWithInitializeIfNotContainsKey(dependsOnMe, bd.getBeanType(), null);
			for (Class<?> dependency : bd.getDependsOn()) {
				putWithInitializeIfNotContainsKey(dependsOnMe, dependency, bd.getBeanType());
			}

			indegree.put(bd.getBeanType(), bd.getDependsOn().length);
		}
	}

	/**
	 * 스프링 기준으로 @Primary 유효성 체크
	 * - 예를들어,
	 * - A 인터페이스를 의존하는 B 클래스가 존재하고
	 * - A 인터페이스를 구현한 C, D 클래스가 빈으로 등록되는 경우 throw NoUniqueBeanDefinitionException
	 * - A 인터페이스를 의존하는 B 클래스가 없다면 C, D 클래스가 빈으로 등록되는 것은 정상적.
	 */
	private void checkPrimaryValidation() {
		for (Map.Entry<Class<?>, List<BeanDefinition>> entry : beanDefinitionMap.entrySet()) {
			List<Class<?>> dom = dependsOnMe.get(entry.getKey());
			if (dom == null || dom.isEmpty()) { // key에 해당하는 클래스를 의존하는 클래스가 없는 경우
				continue;
			}

			int count = 0; // @Primary 가 적용된 클래스 개수
			List<Class<?>> annotatedClasses = new ArrayList<>();
			for (BeanDefinition bd : entry.getValue()) {
				if (bd.isPrimary()) {
					count++;
					annotatedClasses.add(bd.getBeanType());
				}
			}

			if (count > 1) {
				StringBuilder message = new StringBuilder();
				message.append(entry.getKey().getName()).append(" 타입에 @Primary가 적용된 클래스가 2개 이상입니다.\n");

				for (Class<?> annotatedClass : annotatedClasses) {
					message.append("\t- ").append(annotatedClass.getName()).append("\n");
				}

				throw new NoUniqueBeanDefinitionException(message.toString());
			}
		}
	}

	private <T> void putWithInitializeIfNotContainsKey(Map<Class<?>, List<T>> target, Class<?> key, T value) {
		if (!target.containsKey(key)) {
			target.put(key, new ArrayList<>());
		}

		if (value != null) {
			target.get(key).add(value);
		}
	}

	private Class<?>[] getNextClasses(BeanDefinition bd) {
		Set<Class<?>> next = new HashSet<>(dependsOnMe.get(bd.getBeanType()));
		for (Class<?> interfaceType : bd.getInterfaceTypes()) {
			if (dependsOnMe.containsKey(interfaceType)) {
				next.addAll(dependsOnMe.get(interfaceType));
			}
		}

		return next.toArray(new Class<?>[0]);
	}
}
