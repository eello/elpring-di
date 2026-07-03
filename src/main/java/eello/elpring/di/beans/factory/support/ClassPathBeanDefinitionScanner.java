package eello.elpring.di.beans.factory.support;

import eello.elpring.di.annotation.Bean;
import eello.elpring.di.annotation.Component;
import eello.elpring.di.beans.BeanDefinition;
import eello.elpring.di.beans.BeanDefinitionHolder;
import eello.elpring.di.beans.DefaultBeanDefinition;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * basePackages 경로 안에 있는 모든 클래스 중 빈 등록 대상의 클래스들을
 * BeanDefinition 으로 만들고 BeanDefinitionRegistry 에 등록
 */
public class ClassPathBeanDefinitionScanner {

    private static final Logger log = LoggerFactory.getLogger(ClassPathBeanDefinitionScanner.class);

    private final ClassLoader cl;
    private final BeanDefinitionRegistry registry;

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this(Thread.currentThread().getContextClassLoader(),  registry);
    }

    public ClassPathBeanDefinitionScanner(ClassLoader cl, BeanDefinitionRegistry registry) {
        this.cl = cl;
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

        List<BeanDefinition> beanDefinitionList = new ArrayList<>(beanDefinitions);
        for (int i = 0; i < beanDefinitionList.size(); i++) {
            BeanDefinition beanDefinition = beanDefinitionList.get(i);
            String beanName = BeanNameGenerator.generate(beanDefinition);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanName, beanDefinition);
            BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);

            if (beanDefinition.isConfigurationClass()) {
                beanDefinitionList.addAll(parseFactoryBeanMethods(beanDefinitionHolder));
            }
        }

        return new HashSet<>(beanDefinitionList);
    }

    /**
     * BeanDefinitionHolder의 BeanDefinition이 FactoryBean(@Configuration이 적용된 클래스)인 경우
     * 클래스 내부에서 @Bean이 적용된 FactoryBeanMethod를 찾아 BeanDefinition를 만들어 리스트로 반환
     */
    private List<BeanDefinition> parseFactoryBeanMethods(BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        if (!beanDefinition.isConfigurationClass()) {
            return Collections.EMPTY_LIST;
        }

        Class<?> factoryBean = beanDefinition.getBeanType();
        String factoryBeanName = beanDefinitionHolder.getBeanName();

        List<BeanDefinition> factoryBeanMethodDefinitions = new ArrayList<>();
        for (Method method : factoryBean.getMethods()) {
            if (!method.isAnnotationPresent(Bean.class)) {
                continue;
            }

            factoryBeanMethodDefinitions.add(DefaultBeanDefinition.of(method, factoryBeanName));
        }

        return factoryBeanMethodDefinitions;
    }

    private Set<BeanDefinition> findCandidateComponents(String basePackage) throws ClassNotFoundException {
        Set<Class<?>> classes = findAllClasses(basePackage);
        Set<BeanDefinition> beanDefinitions = new HashSet<>();

		for (Class<?> clazz : classes) {
			if (!clazz.isAnnotation() && isComponent(clazz)) {
				if (log.isDebugEnabled()) {
					log.debug("Identified candidate component class: [{}]", clazz.getName());
				}
				beanDefinitions.add(DefaultBeanDefinition.of(clazz));
			}
		}

        return beanDefinitions;
    }

    private Set<Class<?>> findAllClasses(String basePackage) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();

        try {
            String path = basePackage.replace(".", "/");
            java.util.Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.toURI());
                
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                String childPackageName = basePackage + "." + file.getName();
                                classes.addAll(findAllClasses(childPackageName));
                            } else if (file.getName().endsWith(".class")) {
                                String className = basePackage + '.' + file.getName().substring(0, file.getName().length() - 6);
                                classes.add(Class.forName(className));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public void registerConfigClass(List<Class<?>> configClasses) {
        for (Class<?> configClass : configClasses) {
            registerConfigClass(configClass);
        }
    }

    public void registerConfigClass(Class<?> configClass) {
        BeanDefinition configDefinition = DefaultBeanDefinition.of(configClass);
        if (!configDefinition.isConfigurationClass()) {
            throw new IllegalArgumentException(configClass.getName() + " is not a configuration class");
        }

        String configBeanName = BeanNameGenerator.generate(configDefinition);
        BeanDefinitionHolder configBeanDefinitionHolder = new BeanDefinitionHolder(configBeanName, configDefinition);
        BeanDefinitionReaderUtils.registerBeanDefinition(configBeanDefinitionHolder, registry);

        List<BeanDefinition> factoryBeanMethods = parseFactoryBeanMethods(configBeanDefinitionHolder);
        for (BeanDefinition factoryBeanMethod : factoryBeanMethods) {
            String factoryBeanMethodName = BeanNameGenerator.generate(factoryBeanMethod);
            BeanDefinitionHolder factoryBeanDefinitionHolder = new BeanDefinitionHolder(factoryBeanMethodName, factoryBeanMethod);
            BeanDefinitionReaderUtils.registerBeanDefinition(factoryBeanDefinitionHolder, registry);
        }
    }
}
