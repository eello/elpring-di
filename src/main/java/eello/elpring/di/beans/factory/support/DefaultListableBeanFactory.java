package eello.elpring.di.beans.factory.support;

import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.factory.ListableBeanFactory;
import eello.elpring.di.beans.factory.support.registry.DefaultSingletonBeanRegistry;
import eello.elpring.di.beans.factory.support.registry.SingletonBeanRegistry;
import eello.elpring.di.context.ApplicationContext;
import eello.elpring.di.context.ApplicationContextAware;
import eello.elpring.di.exception.*;
import eello.elpring.di.inbox.ResolvableType;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultListableBeanFactory implements ListableBeanFactory, BeanDefinitionRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultListableBeanFactory.class);

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
        if (singletonBeanRegistry.isSingletonCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException("bean name '" + beanName + "'" + " is currently in creation");
        }

        singletonBeanRegistry.setCurrentlyInCreation(beanName);

        if (log.isDebugEnabled()) {
            log.debug("Creating shared instance of singleton bean '{}'", beanName);
        }

        BeanDefinition definition = beanDefinitionMap.get(beanName);
        if (definition.isLazyInit()) {
            if (log.isDebugEnabled()) {
                log.debug("'{}' is lazy loading", beanName);
            }
        }

        Parameter[] dependsOn = definition.getDependsOn();

        Object[] args = new Object[dependsOn.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = resolveParameterDependency(dependsOn[i]);
        }

        Object bean = null;
        try {
            if (definition.isFactoryBeanMethod()) {
                Object factoryBean = getBean(definition.getFactoryBeanName());
                Method factoryMethod = definition.getFactoryMethod();
                bean = factoryMethod.invoke(factoryBean, args);
            } else {
                bean = definition.getConstructors().newInstance(args);
            }

            singletonBeanRegistry.addSingleton(beanName, bean);
            if (bean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to instantiate bean '{}'", beanName, e);
        } finally {
            singletonBeanRegistry.completeCurrentlyInCreation(beanName);
        }

        return bean;
    }

    private Object resolveParameterDependency(Parameter parameter) {
        /*
            ========================================================================
            1차로
                1. 타입이 정확히 일치하는 빈부터 찾아 리턴(해당 타입의 빈 후보가 1개일 때)
                2. 빈 후보가 여러 개일 때 @Primary를 찾아 해당 빈을 리턴
                    2-1. @Primary가 여러 개라면 NoUnique- 예외
                3. @Primary가 적용된 빈이 없다면 빈 이름과 필드 네임(파라미터 이름)이 정확히 일치하는 것을 찾아 리턴
                4. 그마저도 없고 제네릭 타입이 아닌 경우 NoSuch- 예외
            ========================================================================
         */
        if (log.isTraceEnabled()) {
            log.trace("Resolving parameter dependency '{}' for bean...", parameter.getName());
        }
        String requiredBeanName = parameter.getName();

        ResolvableType resolvableType = ResolvableType.forType(parameter.getParameterizedType());
        String[] candidateBeanNames = getBeanNamesForType(resolvableType);

        String beanName = null;
        if (candidateBeanNames.length == 1) {
            beanName = candidateBeanNames[0];
        } else if (candidateBeanNames.length > 1) {
            // 후보가 여러 개인 경우 @Primary를 찾아 리턴
            for (String candidateBeanName : candidateBeanNames) {
                BeanDefinition beanDefinition = getBeanDefinition(candidateBeanName);
                if (beanDefinition.isPrimary()) {
                    if (beanName != null) {
                        throw new NoUniqueBeanDefinitionException("ambiguous for Primary Bean Type '" + beanName + "'");
                    }

                    beanName = candidateBeanName;
                }
            }

            // 빈 이름으로 찾아 리턴
            beanName = Arrays.stream(candidateBeanNames)
                    .filter(requiredBeanName::equals)
                    .findFirst()
                    .orElse(null);
        }

        if (beanName != null) {
            return getBean(beanName);
        }

        if (!isMultiBeanType(resolvableType)) {
            // 배열, Map, List, Set과 같이 빈들을 조립해서 만들 수 있는 자료구조가 아닌 경우
            throw new NoSuchBeanDefinitionException("bean type '" + resolvableType.getType() + "' is not found");
        }

        /*
            ========================================================================
            2차로 requiredType(주입해야할 빈, 찾아야할 빈 타입)이 Array, List, Set인 경우
            제네릭 타입으로 일치하는 빈들을 찾아 각 자료구조의 객체로 만들어 리턴
            ========================================================================
         */
        Object arg = null;
        Class<?> requiredType = resolvableType.toClass();

        if (Map.class.isAssignableFrom(requiredType)) {
            ResolvableType keyType = resolvableType.getGeneric(0);

            if (keyType.toClass() != String.class) {
                throw new IllegalArgumentException("parameter '" + parameter.getName() + "' of type '" + keyType.toClass() +
                        "'" +
                        " is " +
                        "not a string");
            }
        }

        ResolvableType valueType = resolveActualGeneric(resolvableType);
        String[] valueBeanNames = getBeanNamesForType(valueType);
        List<String> validBeanNames = new ArrayList<>();
        for (String name : valueBeanNames) {
            if (!singletonBeanRegistry.isSingletonCurrentlyInCreation(name)) {
                validBeanNames.add(name);
            }
        }
        valueBeanNames = validBeanNames.toArray(new String[0]);

        if (valueBeanNames.length == 0) {
            throw new NoSuchBeanDefinitionException("No beans of type '" + valueType.getType() + "' found for multi-bean type '" + resolvableType.getType() + "'");
        }

        if (resolvableType.isArray()) {
            arg = Array.newInstance(requiredType.getComponentType(), valueBeanNames.length);
            for (int i = 0; i < valueBeanNames.length; i++) {
                Array.set(arg, i, getBean(valueBeanNames[i]));
            }
        }

        if (Map.class.isAssignableFrom(requiredType)) {
            Map<String, Object> mapArg = new HashMap<>();
            for (String valueBeanName : valueBeanNames) {
                mapArg.put(valueBeanName, getBean(valueBeanName));
            }
            arg = mapArg;
        }

        if (Collection.class.isAssignableFrom(requiredType)) {
            Collection<Object> collection = createCollection(requiredType);
            for (String valueBeanName : valueBeanNames) {
                collection.add(getBean(valueBeanName));
            }

            arg = collection;
        }

        if (arg == null) {
            throw new NoSuchBeanDefinitionException("parameter '" + parameter.getName() + "' of type '" + requiredType + "'");
        }

        return arg;
    }

    /*
        요구하는 타입이 Array, Map, Collection(List, Set)과 같은 타입인지 판별
     */
    public boolean isMultiBeanType(ResolvableType resolvableType) {
        return resolvableType.isArray()
                || Map.class.isAssignableFrom(resolvableType.toClass())
                || Collection.class.isAssignableFrom(resolvableType.toClass());
    }

    /*
        Array, Map, Collection(List, Set)의 실제 제네릭 타입을 리턴
     */
    private ResolvableType resolveActualGeneric(ResolvableType resolvedType) {
        if (resolvedType.isArray()) { return resolvedType.getComponentType(); }
        if (Map.class.isAssignableFrom(resolvedType.toClass())) { return resolvedType.getGeneric(1); }
        return resolvedType.getGeneric(0);
    }

    private Collection<Object> createCollection(Class<?> requiredType) {
        if (requiredType.isInterface() || Modifier.isAbstract(requiredType.getModifiers())) {
            if (Set.class.isAssignableFrom(requiredType)) {
                return new HashSet<>();
            }

            return new ArrayList<>();
        }

        try {
            Constructor<?> constructor = requiredType.getDeclaredConstructor();
            constructor.setAccessible(true);
            //noinspection unchecked
            return (Collection<Object>) constructor.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException("Cannot instantiate collection type: " + requiredType, e);
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
        if (log.isDebugEnabled()) {
            log.debug("Registering bean definition [{}] for class [{}]", beanName, beanDefinition.getBeanType().getName());
        }

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
    public String[] getBeanNamesForType(ResolvableType resolvableType) {
        List<String> candidateBeanNames = this.allBeanNamesByType.get(resolvableType.toClass());
        if (candidateBeanNames == null) {
            return new String[0];
        }

        List<String> beanNames = new ArrayList<>();
        for (String candidateBeanName : candidateBeanNames) {
            BeanDefinition definition = this.beanDefinitionMap.get(candidateBeanName);
            ResolvableType targetType = definition.getTargetType();

            if (resolvableType.isAssignableFrom(targetType)) {
                beanNames.add(candidateBeanName);
            }
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
