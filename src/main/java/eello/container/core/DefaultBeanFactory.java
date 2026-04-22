package eello.container.core;

import eello.container.core.registry.SingletonBeanRegistry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultBeanFactory implements BeanFactory {

	private final SingletonBeanRegistry singletonBeanRegistry;
	private final Map<String, BeanDefinition> beanDefinitionMapByName;
	private final Map<Class<?>, List<BeanDefinition>> beanDefinitionMapByType;

	public DefaultBeanFactory(SingletonBeanRegistry singletonBeanRegistry) {
		this.singletonBeanRegistry = singletonBeanRegistry;
		this.beanDefinitionMapByName = new HashMap<>();
		this.beanDefinitionMapByType = new HashMap<>();
	}

	@Override
	public void registerBean(BeanDefinition def) throws
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException {
		if (isRegistered(def)) {
			StringBuilder message = new StringBuilder().append("bean name '")
					.append(def.getBeanName())
					.append("'은 이미 등록되어 있는 빈.");

			throw new IllegalArgumentException(message.toString());
		}

		// bean instance 생성
		Object bean = createBean(def);

		// 싱글톤 빈 등록
		singletonBeanRegistry.addSingleton(def.getBeanName(), bean);

		// 빈의 이름에 BeanDefinition 매핑
		beanDefinitionMapByName.put(def.getBeanName(), def);

		// 빈의 인터페이스 타입에 BeanDefinition 매핑
		putToBeanDefinitionMapByType(def.getBeanType(), def);
		for (Class<?> interfaceType : def.getInterfaceTypes()) {
			putToBeanDefinitionMapByType(interfaceType, def);
		}

		StringBuilder log = new StringBuilder().append("[")
				.append(def.getBeanTypeName())
				.append("] - '")
				.append(def.getBeanName())
				.append("' 등록.");

		System.out.println(log);
	}

	private Object createBean(BeanDefinition def) throws
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException {
		Class<?>[] dependsOn = def.getDependsOn();
		Object[] args = new Object[dependsOn.length];

		for (int i = 0; i < dependsOn.length; i++) {
			List<BeanDefinition> candidates = beanDefinitionMapByType.get(dependsOn[i]);
			if (candidates == null || candidates.isEmpty()) {
				StringBuilder message = new StringBuilder();
				message.append("'").append(def.getBeanName()).append("'")
						.append(" - [").append(def.getBeanTypeName()).append("] 타입의 빈을 생성하기 위해 필요한 빈이 등록되지 않음.")
						.append("\n")
						.append("\t - ").append(dependsOn[i].getName())
						.append("\n");

				throw new IllegalStateException(message.toString());
			}

			if (candidates.size() == 1) {
				// 후보 빈이 1개인 경우 해당 빈을 주입
				args[i] = singletonBeanRegistry.getSingleton(candidates.getFirst().getBeanName());
			} else {
				// 후보 빈이 여러개인 경우
				for (BeanDefinition candidate : candidates) {
					if (candidate.isPrimary()) {
						// @Primary 가 적용된 빈을 주입
						args[i] = singletonBeanRegistry.getSingleton(candidate.getBeanName());
						break;
					}
				}

				if (args[i] == null) {
					// 후보 빈이 여러개이면서 @Primary 도 적용되지 않은 경우 빈 주입에 모호성 발생
					StringBuilder message = new StringBuilder();
					message.append(dependsOn[i]).append(" 타입에 해당하는 빈이 2개 이상 등록되어 주입에 모호함.\n");

					for (BeanDefinition candidate : candidates) {
						message.append("\t- ").append(candidate.getBeanType()).append("\n");
					}

					throw new IllegalStateException(message.toString());
				}
			}
		}

		return def.getConstructors().newInstance(args);
	}

	@Override
	public Object getBean(String beanName) {
		return singletonBeanRegistry.getSingleton(beanName);
	}

	@Override
	public <T> T getBean(String beanName, Class<T> requiredType) {
		return (T) singletonBeanRegistry.getSingleton(beanName);
	}

	@Override
	public <T> T[] getBean(Class<T> beanType) {
		List<BeanDefinition> beanDefinitions = beanDefinitionMapByType.get(beanType);

		List<T> find = new ArrayList<>();
		for (BeanDefinition beanDefinition : beanDefinitions) {
			find.add(getBean(beanDefinition.getBeanName(), beanType));
		}

		return find.toArray((T[]) new Object[0]);
	}

	@Override
	public boolean isRegistered(String beanName) {
		return beanDefinitionMapByName.containsKey(beanName);
	}

	@Override
	public boolean isRegistered(BeanDefinition def) {
		return isRegistered(def.getBeanName());
	}

	private void putToBeanDefinitionMapByType(Class<?> beanType, BeanDefinition beanDefinition) {
		if (!beanDefinitionMapByType.containsKey(beanType)) {
			beanDefinitionMapByType.put(beanType, new ArrayList<>());
		}

		beanDefinitionMapByType.get(beanType).add(beanDefinition);
	}

}
