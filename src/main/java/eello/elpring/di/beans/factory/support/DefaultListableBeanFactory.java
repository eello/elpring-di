package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.factory.ListableBeanFactory;
import eello.elpring.di.beans.factory.support.registry.DefaultSingletonBeanRegistry;
import eello.elpring.di.beans.factory.support.registry.SingletonBeanRegistry;
import eello.elpring.di.exception.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory implements ListableBeanFactory, BeanDefinitionRegistry {

    private final SingletonBeanRegistry singletonBeanRegistry;
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private final Map<Class<?>, List<String>> allBeanNamesByType = new ConcurrentHashMap<>();

    public DefaultListableBeanFactory() {
        this(new DefaultSingletonBeanRegistry());
    }

    public DefaultListableBeanFactory(SingletonBeanRegistry singletonBeanRegistry) {
        this.singletonBeanRegistry = singletonBeanRegistry;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        if (!beanDefinitionMap.containsKey(name)) {
            throw new NoSuchBeanDefinitionException("bean name '" + name + "'" + " not found");
        }

        Object bean = singletonBeanRegistry.getSingleton(name);
        if (bean != null) {
            return bean;
        }

        bean = instantiateBean(name);

        return bean;
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        Object bean = getBean(name);
        return (T) bean;
    }

    /*
        requiredType 에 해당 하는 빈 중 우선순위가 가장 높은 빈 반환
        BeanDefinition 은 있지만 인스턴스가 없다면 getBean(beanName) 으로 생성 및 등록 후 반환
     */
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        List<String> candidateBeanNames = this.allBeanNamesByType.get(requiredType);
        if (candidateBeanNames == null) {
            throw new NoSuchBeanDefinitionException("bean type '" + requiredType + "'" + " not found");
        }

        String beanName = null;
        if (candidateBeanNames.size() == 1) {
            beanName = candidateBeanNames.getFirst();
        } else {
            String primaryBeanName = null;
            for (String candidateBeanName : candidateBeanNames) {
                BeanDefinition candidateDefinition = this.beanDefinitionMap.get(candidateBeanName);
                if (candidateDefinition.isPrimary()) {
                    if (primaryBeanName != null) {
                        throw new NoUniqueBeanDefinitionException("ambiguous for Primary Bean Type '" + candidateDefinition.getBeanType() + "'");
                    }
                    primaryBeanName = candidateBeanName;
                }
            }

            if (primaryBeanName != null) {
                beanName = primaryBeanName;
            } else {
                throw new NoUniqueBeanDefinitionException("ambiguous for Bean Type '" + requiredType + "'");
            }
        }

        return (T) getBean(beanName);
    }

    private Object instantiateBean(String beanName) throws BeansException {
        if (singletonBeanRegistry.isSingletonCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException("bean name '" + beanName + "'" + " is currently in creation");
        }

        singletonBeanRegistry.setCurrentlyInCreation(beanName);

        BeanDefinition definition = beanDefinitionMap.get(beanName);
        if (definition.isLazyInit()) {
            System.out.println("'" + beanName + "' is lazy loading");
        }

        Class<?>[] dependsOn = definition.getDependsOn();

        int argCount = 0;
        Object[] constructorArgs = new Object[dependsOn.length];

        for (Class<?> clazz : dependsOn) {
            constructorArgs[argCount++] = this.getBean(clazz);
        }

        try {
            Object bean = definition.getConstructors().newInstance(constructorArgs);

            singletonBeanRegistry.addSingleton(beanName, bean);
            singletonBeanRegistry.completeCurrentlyInCreation(beanName);

            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        // TODO 추후 구현: 구현하기 위해서 BeanDefinition 을 만드는 과정에서 Annotation 들을 모아서 저장해야할 듯
        return Map.of();
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        BeanDefinition existingDefinition = this.getBeanDefinition(beanName);
        if (existingDefinition != null) {
            if (existingDefinition.equals(beanDefinition)) {
                // 동일한 beanDefinition이 스캔된 경우 skip
                return;
            }

            throw new BeanDefinitionStoreException("Bean with name '" + beanName + "' already exists");
        }

        beanDefinitionMap.put(beanName, beanDefinition);
        // 등록될 빈의 타입을 포함한 모든 상위 타입 조회
        for (Class<?> type : collectTypeHierarchy(beanDefinition.getBeanType())) {
            this.allBeanNamesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(beanName);
        }
    }

    // clazz 의 모든 상위 타입 반환
    private Set<Class<?>> collectTypeHierarchy(Class<?> clazz) {
        Set<Class<?>> types = new HashSet<>();
        if (clazz == null) {
            return types;
        }

        types.add(clazz);

        for (Class<?> iface : clazz.getInterfaces()) {
            types.addAll(collectTypeHierarchy(iface));
        }

        types.addAll(collectTypeHierarchy(clazz.getSuperclass()));

        return types;
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }

    /*
        모든 빈 이름을 문자열 배열로 리턴
     */
    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[0]);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }
}
