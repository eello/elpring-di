package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.factory.ListableBeanFactory;
import eello.elpring.di.beans.factory.support.registry.DefaultSingletonBeanRegistry;
import eello.elpring.di.beans.factory.support.registry.SingletonBeanRegistry;
import eello.elpring.di.context.ApplicationContext;
import eello.elpring.di.context.ApplicationContextAware;
import eello.elpring.di.exception.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static eello.elpring.di.util.GenericTypeResolver.*;

public class DefaultListableBeanFactory implements ListableBeanFactory, BeanDefinitionRegistry {

    private ApplicationContext applicationContext;
    private final SingletonBeanRegistry singletonBeanRegistry;
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    private final Map<Class<?>, List<String>> allBeanNamesByType = new ConcurrentHashMap<>();

    public DefaultListableBeanFactory() {
        this(new DefaultSingletonBeanRegistry());
    }

    public DefaultListableBeanFactory(SingletonBeanRegistry singletonBeanRegistry) {
        this.singletonBeanRegistry = singletonBeanRegistry;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
        /*
            1단계 제네릭 타입까지 처리 가능 ex) List<InterfaceA>: O, List<List<InterfaceA>> X
            제네릭이 중첩된 경우 throw IllegalStateException
            TODO: 제네릭 중첩 해결
         */
        if (singletonBeanRegistry.isSingletonCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException("bean name '" + beanName + "'" + " is currently in creation");
        }

        singletonBeanRegistry.setCurrentlyInCreation(beanName);

        BeanDefinition definition = beanDefinitionMap.get(beanName);
        if (definition.isLazyInit()) {
            System.out.println("'" + beanName + "' is lazy loading");
        }

        Parameter[] dependsOn = definition.getDependsOn();

        int argCount = 0;
        Object[] constructorArgs = new Object[dependsOn.length];

        for (Parameter param : dependsOn) {
            String paramName = param.getName(); // == beanName
            Class<?> paramType = param.getType();

            Object arg;
            if (paramType.equals(Map.class)) {
                // Key가 String이 아니라면 예외 발생
                Class<?> keyType = getGenericKeyType(param);
                assert keyType != null;
                if (!keyType.equals(String.class)) {
                    throw new IllegalArgumentException("parameter '" + param.getName() + "' of type '" + keyType + "'" +
                            " is " +
                            "not a string");
                }

                Class<?> valueType = getGenericValueType(param);
                assert valueType != null;
                arg = getBeansOfType(valueType);
            } else if (paramType.isArray() || paramType.equals(List.class) || paramType.equals(Set.class)) {
                Class<?> componentType;
                if (paramType.isArray()) {
                    componentType = paramType.getComponentType();
                    Collection<?> beans = getBeansOfType(componentType).values();
                    arg = Array.newInstance(componentType, beans.size());

                    int arrIndex = 0;
                    for (Object bean : beans) {
                        Array.set(arg, arrIndex++, componentType.cast(bean));
                    }
                } else { // paramType이 List 혹은 Set인 경우
                    componentType = getGenericComponentType(param);
                    Collection<?> beans = getBeansOfType(componentType).values();

                    if (paramType.equals(List.class)) {
                        arg = new ArrayList<>(beans);
                    } else arg = new HashSet<>(beans);
                }
            } else {
                try {
                    arg = getBean(paramType);
                } catch (NoUniqueBeanDefinitionException e) {
                    try {
                        arg = getBean(paramName);
                    } catch (NoSuchBeanDefinitionException e2) {
                        throw e2;
                    }
                }
            }

            constructorArgs[argCount++] = arg;
        }

        try {
            Object bean = definition.getConstructors().newInstance(constructorArgs);
            if (bean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
            }

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
        Map<String, Object> beansWithAnnotation = new HashMap<>();

        for (Map.Entry<String, BeanDefinition> definitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = definitionEntry.getKey();
            BeanDefinition definition = definitionEntry.getValue();

            if (definition.hasAnnotation(annotationType)) {
                beansWithAnnotation.put(beanName, getBean(beanName));
            }
        }

        return beansWithAnnotation;
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

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        List<String> beanNames = allBeanNamesByType.get(type);
        if (beanNames == null) {
            return new String[0];
        }

        return beanNames.toArray(new String[0]);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> beansOfType = new HashMap<>();

        String[] beanNames = getBeanNamesForType(type);
        for (String beanName : beanNames) {
            if (!singletonBeanRegistry.isSingletonCurrentlyInCreation(beanName)) {
                beansOfType.put(beanName, this.getBean(beanName, type));
            }
        }

        return beansOfType;
    }
}
